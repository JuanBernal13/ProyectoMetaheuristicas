import json
import pulp
import numpy as np
from typing import Dict, List, Tuple
import time
import math

class ModeloOptimizacionVehiculos:
    def __init__(self, archivo_json: str):
        """
        Inicializar el modelo de optimización de vehículos eléctricos
        """
        self.archivo_json = archivo_json
        self.datos = self.cargar_datos()
        self.preprocesar_datos()
        self.crear_modelo()
        
    def cargar_datos(self) -> Dict:
        """Cargar datos del archivo JSON"""
        with open(self.archivo_json, 'r') as f:
            return json.load(f)
    
    def preprocesar_datos(self):
        """Preprocesamiento y transformación de datos desde el archivo JSON"""
        # Obtener configuración del parking
        parking_config = self.datos["parking_config"]
        
        # Discretización del tiempo usando la resolución del JSON
        self.tiempo_resolucion = 0.25 # 0.25 horas
        self.precios_energia = self.datos["energy_prices"]
        self.tiempos = [p["time"] for p in self.precios_energia]
        self.intervalos_tiempo = list(range(len(self.tiempos)))
        self.duracion_intervalo = self.tiempo_resolucion
        
        # Mapeo de precios (convertir de cents/kWh a $/kWh)
        self.precios = {t: self.precios_energia[t]["price"] / 100.0 for t in self.intervalos_tiempo}
        
        # Vehículos (usar todos los vehículos del JSON)
        self.vehiculos = self.datos["arrivals"]
        self.ids_vehiculos = [v["id"] for v in self.vehiculos]
        
        # Cargadores (usar los cargadores del JSON)
        self.cargadores = parking_config["chargers"]
        self.ids_cargadores = [c["charger_id"] for c in self.cargadores]
        
        # Crear un diccionario para acceder a los datos del vehículo por ID
        self.vehiculos_dict = {v["id"]: v for v in self.vehiculos}
        
        # Parámetros del sistema extraídos del JSON
        self.limite_transformador = parking_config["transformer_limit"]  # 300 kW
        self.n_plazas = parking_config["n_spots"]  # 30 plazas
        self.eficiencia_sistema = parking_config["efficiency"]  # 0.9
        
        # Restricciones de red extraídas del JSON
        grid_constraints = parking_config["grid_constraints"]
        self.potencia_max_fase = grid_constraints["max_power_per_phase"]  # 100.0 kW
        self.limite_caida_voltaje = grid_constraints["voltage_drop_limit"]  # 0.05
        self.factor_potencia_min = grid_constraints["power_factor_limit"]  # 0.95
        
        # Información de marcas de vehículos
        self.car_brands = self.datos["car_brands"]
        self.charger_types = self.datos["charger_types"]
        
        # Calcular prioridades normalizadas (1-10)
        self.calcular_prioridades_normalizadas()
        
        # Calcular eficiencias de carga desde el JSON
        self.calcular_eficiencias_desde_json()
        
        # Calcular máximo retraso permitido
        self.calcular_max_retrasos()
        
        # Calcular compatibilidades desde el JSON
        self.calcular_compatibilidades_desde_json()
        
        print(f"- Vehículos: {len(self.vehiculos)}")
        print(f"- Cargadores: {len(self.cargadores)}")
        print(f"- Intervalos de tiempo: {len(self.intervalos_tiempo)} (resolución {self.duracion_intervalo}h)")
        print(f"- Duración total: {max(self.tiempos):.2f} horas")
        print(f"- Límite transformador: {self.limite_transformador} kW")
        print(f"- Plazas disponibles: {self.n_plazas}")
        print(f"- Potencia máxima por fase: {self.potencia_max_fase} kW")
    
    def calcular_prioridades_normalizadas(self):
        """Calcular prioridades normalizadas de 1-10"""
        self.prioridades = {}
        for vehiculo in self.vehiculos:
            v_id = vehiculo["id"]
            prioridad_original = vehiculo["priority"]
            tiempo_estancia = vehiculo["departure_time"] - vehiculo["arrival_time"]
            energia_requerida = vehiculo["required_energy"]
            
            # Presión temporal
            presion_temporal = energia_requerida / tiempo_estancia if tiempo_estancia > 0 else 1.0
            
            # Escalar prioridad (1->1-3, 2->4-6, 3->7-10)
            if prioridad_original == 1:
                prioridad_base = 2
            elif prioridad_original == 2:
                prioridad_base = 5
            else:  # prioridad_original == 3
                prioridad_base = 8
                
            # Ajustar por presión temporal (normalizado a 1-10)
            factor_presion = min(2.0, presion_temporal / 10.0)
            prioridad_normalizada = min(10, max(1, prioridad_base + factor_presion))
            
            self.prioridades[v_id] = prioridad_normalizada
    
    def calcular_eficiencias_desde_json(self):
        """Calcular eficiencias de carga desde el JSON"""
        self.eficiencias_vehiculos = {}
        for vehiculo in self.vehiculos:
            v_id = vehiculo["id"]
            self.eficiencias_vehiculos[v_id] = vehiculo["efficiency"]
            
        self.eficiencias_cargadores = {}
        for cargador in self.cargadores:
            c_id = cargador["charger_id"]
            self.eficiencias_cargadores[c_id] = cargador["efficiency"]
    
    def calcular_max_retrasos(self):
        """Calcular máximo retraso permitido basado en el tiempo de estancia"""
        self.max_retrasos = {}
        for vehiculo in self.vehiculos:
            v_id = vehiculo["id"]
            tiempo_estancia = vehiculo["departure_time"] - vehiculo["arrival_time"]
            # Retraso basado en el tiempo de estancia (15% del tiempo disponible, máximo 2 horas)
            max_retraso = min(2.0, 0.15 * tiempo_estancia)
            self.max_retrasos[v_id] = max_retraso
    
    def calcular_compatibilidades_desde_json(self):
        """Calcular compatibilidades vehículo-cargador desde el JSON"""
        self.compatibilidades = {}
        
        for vehiculo in self.vehiculos:
            v_id = vehiculo["id"]
            marca_vehiculo = vehiculo["brand"]
            
            # Extraer marca base para comparación
            marca_base = marca_vehiculo.split()[0] + " " + marca_vehiculo.split()[1]
            
            for cargador in self.cargadores:
                c_id = cargador["charger_id"]
                vehiculos_compatibles = cargador["compatible_vehicles"]
                
                # Verificar compatibilidad
                compatible = False
                for vc in vehiculos_compatibles:
                    if vc in marca_vehiculo or marca_base.startswith(vc) or vc in marca_base:
                        compatible = True
                        break
                
                self.compatibilidades[(v_id, c_id)] = compatible
    
    def crear_modelo(self):
        """Crear el modelo de optimización con PuLP"""
        print("Creando modelo de optimización...")
        
        # Crear problema
        self.modelo = pulp.LpProblem("Optimizacion_Vehiculos_Electricos", pulp.LpMinimize)
        
        # Variables de decisión
        self.crear_variables()
        
        # Función objetivo
        self.definir_funcion_objetivo()
        
        # Restricciones
        self.agregar_restricciones()
        
        print("Modelo creado exitosamente")
    
    def crear_variables(self):
        """Crear todas las variables de decisión (simplificadas)"""
        V = self.ids_vehiculos
        T = self.intervalos_tiempo
        C = self.ids_cargadores
        
        # Variable x_{i,t,c}: asignación de cargador (binaria)
        self.x = pulp.LpVariable.dicts("x", 
                                      [(i, t, c) for i in V for t in T for c in C],
                                      cat='Binary')
        
        # Variable P_{i,t,c}: potencia de carga asignada (continua)
        self.P = pulp.LpVariable.dicts("P", 
                                      [(i, t, c) for i in V for t in T for c in C],
                                      lowBound=0,
                                      cat='Continuous')
        
        # Variable E_i: energía entregada (continua)
        self.E = pulp.LpVariable.dicts("E", V, lowBound=0, cat='Continuous')
        
        # Variable y_{i,t}: indicador de uso de cargador (binaria)
        self.y = pulp.LpVariable.dicts("y", 
                                      [(i, t) for i in V for t in T],
                                      cat='Binary')
        
        # Variable slack_pago_excedente_i: holgura para la disposición a pagar (continua)
        self.slack_pago_excedente = pulp.LpVariable.dicts("slack_pago_excedente", 
                                                        V, 
                                                        lowBound=0,
                                                        cat='Continuous')
    
    def definir_funcion_objetivo(self):
        """Definir la función objetivo usando datos del JSON"""
        V = self.ids_vehiculos
        T = self.intervalos_tiempo
        C = self.ids_cargadores
        
        # Función objetivo multiobjetivo usando datos reales del JSON
        
        # 1. Costo de energía
        costo_energia = pulp.lpSum([
            self.P[(i, t, c)] * self.duracion_intervalo * self.precios[t]
            for i in V for t in T for c in C
        ])
        
        # 2. Costo operativo de cargadores (desde JSON)
        costo_operativo = pulp.lpSum([
            self.x[(i, t, c)] * self.duracion_intervalo * 
            self.cargadores[c]["operation_cost_per_hour"]
            for i in V for t in T for c in C
        ])
        
        # 3. Energía entregada (para maximizar)
        energia_entregada = pulp.lpSum([self.E[i] for i in V])
        
        # 4. Bonificación por prioridad
        bonificacion_prioridad = pulp.lpSum([
            0.1 * self.prioridades[i] * self.E[i]
            for i in V
        ])
        
        # 5. Penalización por excedente de disposición a pagar (slack)
        penalizacion_slack_pago = pulp.lpSum([2.0 * self.slack_pago_excedente[i] for i in V])
        
        # Función objetivo: minimizar costos y maximizar eficiencia
        costo_total = costo_energia + costo_operativo
        eficiencia_total = energia_entregada + bonificacion_prioridad
        
        self.modelo += 1.0 * costo_total - 0.5 * eficiencia_total + penalizacion_slack_pago
    
    def agregar_restricciones(self):
        """Agregar todas las restricciones del modelo (simplificadas)"""
        print("Agregando restricciones básicas...")
        
        self.restricciones_capacidad_basicas()
        self.restricciones_asignacion_basicas()
        self.restricciones_potencia_energia_basicas()
        
        print("Restricciones básicas agregadas")
    
    def restricciones_capacidad_basicas(self):
        """Restricciones de capacidad del sistema usando parámetros del JSON"""
        V = self.ids_vehiculos
        T = self.intervalos_tiempo
        C = self.ids_cargadores
        
        # Límite del transformador (desde JSON)
        for t in T:
            self.modelo += (
                pulp.lpSum([self.P[(i, t, c)] for i in V for c in C]) <= self.limite_transformador,
                f"Limite_Transformador_{t}"
            )
        
        # Límite de plazas de estacionamiento (desde JSON)
        for t in T:
            self.modelo += (
                pulp.lpSum([self.y[(i, t)] for i in V]) <= self.n_plazas,
                f"Limite_Plazas_{t}"
            )
        
        # Límite de potencia por fase (desde JSON)
        for t in T:
            self.modelo += (
                pulp.lpSum([self.P[(i, t, c)] for i in V for c in C]) / 3 <= self.potencia_max_fase,
                f"Limite_Potencia_Fase_{t}"
            )
    
    def restricciones_asignacion_basicas(self):
        """Restricciones de asignación y compatibilidad usando datos del JSON"""
        V = self.ids_vehiculos
        T = self.intervalos_tiempo
        C = self.ids_cargadores
        
        # Un vehículo por cargador
        for t in T:
            for c in C:
                self.modelo += (
                    pulp.lpSum([self.x[(i, t, c)] for i in V]) <= 1,
                    f"Un_Vehiculo_Por_Cargador_{t}_{c}"
                )
        
        # Un cargador por vehículo
        for i in V:
            for t in T:
                self.modelo += (
                    pulp.lpSum([self.x[(i, t, c)] for c in C]) == self.y[(i, t)],
                    f"Un_Cargador_Por_Vehiculo_{i}_{t}"
                )
        
        # Compatibilidad vehículo-cargador (desde JSON)
        for i in V:
            for t in T:
                for c in C:
                    if not self.compatibilidades.get((i, c), False):
                        self.modelo += (
                            self.x[(i, t, c)] == 0,
                            f"Compatibilidad_{i}_{t}_{c}"
                        )
        
        # Ventana temporal
        for i in V:
            vehiculo = self.vehiculos_dict[i]
            llegada = vehiculo["arrival_time"]
            salida = vehiculo["departure_time"]
            max_retraso = self.max_retrasos[i]
            
            for t in T:
                tiempo_inicio = self.tiempos[t]
                tiempo_fin = tiempo_inicio + self.duracion_intervalo
                
                # No puede cargar antes de llegar o después de salir + retraso
                if tiempo_fin <= llegada or tiempo_inicio >= salida + max_retraso:
                    self.modelo += (
                        self.y[(i, t)] == 0,
                        f"Ventana_Temporal_{i}_{t}"
                    )
    
    def restricciones_potencia_energia_basicas(self):
        """Restricciones de potencia y energía usando datos del JSON"""
        V = self.ids_vehiculos
        T = self.intervalos_tiempo
        C = self.ids_cargadores
        
        # Potencia asignada - límites desde JSON
        for i in V:
            vehiculo = self.vehiculos_dict[i]
            for t in T:
                for c in C:
                    cargador = self.cargadores[c]
                    
                    # Límite superior basado en las capacidades reales
                    if cargador["type"] == "AC":
                        potencia_max = min(cargador["power"], vehiculo["ac_charge_rate"])
                    else:  # DC
                        potencia_max = min(cargador["power"], vehiculo["dc_charge_rate"])
                    
                    self.modelo += (
                        self.P[(i, t, c)] <= potencia_max * self.x[(i, t, c)],
                        f"Potencia_Max_{i}_{t}_{c}"
                    )
                    
                    # Límite inferior
                    potencia_min = max(1.0, vehiculo["min_charge_rate"])
                    self.modelo += (
                        self.P[(i, t, c)] >= potencia_min * self.x[(i, t, c)],
                        f"Potencia_Min_{i}_{t}_{c}"
                    )
        
        # Energía entregada usando eficiencias reales del JSON
        for i in V:
            self.modelo += (
                self.E[i] == pulp.lpSum([
                    self.P[(i, t, c)] * self.duracion_intervalo * 
                    self.eficiencias_vehiculos[i] * self.eficiencias_cargadores[c]
                    for t in T for c in C
                ]),
                f"Energia_Entregada_{i}"
            )
        
        # Límite de demanda
        for i in V:
            vehiculo = self.vehiculos_dict[i]
            self.modelo += (
                self.E[i] <= vehiculo["required_energy"],
                f"Limite_Demanda_{i}"
            )
        
        # Límite de disposición a pagar
        for i in V:
            vehiculo = self.vehiculos_dict[i]
            self.modelo += (
                pulp.lpSum([
                    self.P[(i, t, c)] * self.duracion_intervalo * self.precios[t]
                    for t in T for c in C
                ]) <= vehiculo["willingness_to_pay"] * vehiculo["required_energy"] + self.slack_pago_excedente[i],
                f"Limite_Disposicion_Pagar_{i}"
            )
    
    def resolver(self, tiempo_limite=20):
        """Resolver el modelo de optimización usando datos del JSON"""
        print(f"Iniciando resolución del modelo completo con {len(self.vehiculos)} vehículos")
        print(f"Límite de tiempo: {tiempo_limite} segundos")
        
        # Configurar solver con opciones compatibles
        solver = pulp.PULP_CBC_CMD(
            timeLimit=tiempo_limite, 
            msg=1
        )
        
        # Resolver
        inicio = time.time()
        self.modelo.solve(solver)
        tiempo_resolucion = time.time() - inicio
        
        # Verificar estado
        estado = pulp.LpStatus[self.modelo.status]
        print(f"Estado de la solución: {estado}")
        print(f"Tiempo de resolución: {tiempo_resolucion:.2f} segundos")
        
        if self.modelo.status == pulp.LpStatusOptimal or self.modelo.status == pulp.LpStatusNotSolved:
            self.mostrar_resultados()
            return True
        else:
            print("No se encontró solución factible")
            return False
    
    def mostrar_resultados(self):
        """Mostrar resultados detallados de la optimización"""
        if self.modelo.status not in [pulp.LpStatusOptimal, pulp.LpStatusNotSolved]:
            print("No hay solución para mostrar")
            return
        
        print("\n" + "="*80)
        print("RESULTADOS DE LA OPTIMIZACIÓN - TEST_SYSTEM_3")
        print("="*80)
        
        # Valor objetivo
        if self.modelo.objective.value() is not None:
            print(f"Valor objetivo: {self.modelo.objective.value():.2f}")
        
        # Estadísticas de vehículos
        total_energia_entregada = 0
        total_energia_requerida = 0
        vehiculos_atendidos = 0
        total_costo = 0
        
        # Desglose de costos
        total_costo_energia = 0
        total_costo_operativo_cargadores = 0
        
        print("\nRESUMEN POR VEHÍCULO:")
        print("-" * 100)
        print(f"{'ID':<3} {'Marca':<20} {'Energía Req.':<12} {'Energía Ent.':<12} {'Satisfacción':<12} {'Prioridad':<9}")
        print("-" * 100)
        
        for i in self.ids_vehiculos:
            vehiculo = self.vehiculos_dict[i]
            energia_requerida = vehiculo["required_energy"]
            energia_entregada = self.E[i].value() if self.E[i].value() else 0
            satisfaccion = (energia_entregada / energia_requerida * 100) if energia_requerida > 0 else 0
            marca = vehiculo["brand"][:20]
            
            if energia_entregada > 0.01:
                vehiculos_atendidos += 1
            
            total_energia_entregada += energia_entregada
            total_energia_requerida += energia_requerida
            
            # Calcular costo individual y agregarlos a los totales desglosados
            costo_individual_energia = 0
            costo_individual_operativo = 0
            for t in self.intervalos_tiempo:
                for c in self.ids_cargadores:
                    if self.P.get((i, t, c)) and self.P[(i, t, c)].value():
                        potencia = self.P[(i, t, c)].value()
                        costo_energia_intervalo = potencia * self.duracion_intervalo * self.precios[t]
                        costo_operativo_intervalo = self.x[(i, t, c)].value() * self.duracion_intervalo * self.cargadores[c]["operation_cost_per_hour"]
                        
                        costo_individual_energia += costo_energia_intervalo
                        costo_individual_operativo += costo_operativo_intervalo
            
            total_costo_energia += costo_individual_energia
            total_costo_operativo_cargadores += costo_individual_operativo
            total_costo += (costo_individual_energia + costo_individual_operativo)
            
            print(f"{i:<3} {marca:<20} {energia_requerida:<12.2f} {energia_entregada:<12.2f} {satisfaccion:<12.1f}% {vehiculo['priority']:<9}")
        
        print("-" * 100)
        print(f"Totales: {total_energia_requerida:.2f} kWh → {total_energia_entregada:.2f} kWh")
        
        # Estadísticas generales
        print(f"\nESTADÍSTICAS GENERALES:")
        print(f"- Vehículos atendidos: {vehiculos_atendidos}/{len(self.vehiculos)}")
        print(f"- Satisfacción promedio: {(total_energia_entregada/total_energia_requerida*100):.1f}%")
        print(f"- Costo total estimado (Energía + Operación Cargadores): ${total_costo:.2f}")
        print(f"  - Costo de Energía: ${total_costo_energia:.2f}")
        print(f"  - Costo Operativo de Cargadores: ${total_costo_operativo_cargadores:.2f}")
        print(f"- Límite del transformador: {self.limite_transformador} kW")
        print(f"- Plazas utilizadas máximo: {self.n_plazas}")
        
        # Análisis de cargadores
        print(f"\nANÁLISIS DE CARGADORES:")
        print("-" * 80)
        print(f"{'ID':<3} {'Tipo':<4} {'Potencia':<8} {'Costo/h':<8} {'Uso (%)':<8} {'Compatible con':<20}")
        print("-" * 80)
        
        for c in self.ids_cargadores:
            cargador = self.cargadores[c]
            uso_total = 0
            for i in self.ids_vehiculos:
                for t in self.intervalos_tiempo:
                    if self.x.get((i, t, c)) and self.x[(i, t, c)].value():
                        uso_total += 1
            
            uso_porcentaje = (uso_total / len(self.intervalos_tiempo)) * 100
            compatibles = len(cargador["compatible_vehicles"])
            
            print(f"{c:<3} {cargador['type']:<4} {cargador['power']:<8} ${cargador['operation_cost_per_hour']:<7.2f} {uso_porcentaje:<8.1f} {compatibles:<20}")
        
        print("-" * 80)


# Función principal
def main():
    """Función principal para ejecutar el modelo con datos del test_system_3.json"""
    while True:
        try:
            instance_to_run = input("Introduce el número de la instancia a correr (ej. 1, 2, ..., 7, o 'all' para todas): ")
            if instance_to_run.lower() == 'all':
                run_all_tests(run_all=True)
                break
            else:
                instance_num = int(instance_to_run)
                if 1 <= instance_num <= 7:
                    run_all_tests(instance_number=instance_num)
                    break
                else:
                    print("Número de instancia inválido. Debe ser entre 1 y 7, o 'all'.")
        except ValueError:
            print("Entrada inválida. Por favor, introduce un número o 'all'.")

def run_all_tests(instance_number: int = None, run_all: bool = False):
    results = {}
    
    if run_all:
        instances = range(1, 8)
    elif instance_number is not None:
        instances = [instance_number]
    else:
        print("Error: Se debe especificar un número de instancia o 'all'.")
        return

    for i in instances:
        json_file = f"test_system_{i}.json"
        print(f"\nIniciando optimización para {json_file}")
        try:
            modelo = ModeloOptimizacionVehiculos(json_file)
            exito = modelo.resolver(tiempo_limite=60) # Aumentar límite de tiempo para modelos más grandes

            if exito and modelo.modelo.status == pulp.LpStatusOptimal and modelo.modelo.objective.value() is not None:
                results[json_file] = {
                    "objective_value": modelo.modelo.objective.value(),
                    "status": pulp.LpStatus[modelo.modelo.status]
                }
                print(f"Resultado para {json_file}: Valor Objetivo = {modelo.modelo.objective.value():.2f}")
            else:
                results[json_file] = {
                    "objective_value": None,
                    "status": pulp.LpStatus[modelo.modelo.status]
                }
                print(f"No se pudo obtener un valor objetivo óptimo para {json_file}. Estado: {pulp.LpStatus[modelo.modelo.status]}")

        except FileNotFoundError:
            print(f"Error: Archivo {json_file} no encontrado. Saltando este test.")
            results[json_file] = {"objective_value": None, "status": "FileNotFound"}
        except Exception as e:
            print(f"Ocurrió un error al procesar {json_file}: {e}")
            results[json_file] = {"objective_value": None, "status": f"Error: {e}"}

    # Guardar resultados en un archivo JSON
    output_file = "optimization_results.json"
    with open(output_file, 'w') as f:
        json.dump(results, f, indent=4)
    print(f"\nResultados de la optimización guardados en {output_file}")

if __name__ == "__main__":
    main()
