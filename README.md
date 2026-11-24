# IscoT_PCD

Projeto de **Programação Concorrente e Distribuída (PCD)** — 2026/27.

O projeto modela um sistema de gestão doméstica através de **domótica**. Existem sensores, decisores e atuadores que se ligam a um servidor central, o **IscoT**, um *message broker* que gere toda a comunicação do sistema distribuído, usando um protocolo próprio.

---

## Arquitetura

O servidor mantém **filas de eventos por tópico**. Os clientes ligam-se remotamente e publicam e/ou consomem eventos.

```
Sensores ──(SensorEvent)──► IscoT ──(SensorEvent)──► Decisores
                              ▲                           │
                              └───(HighLevelOrderEvent)───┘
                              │
                              └──(HighLevelOrderEvent)──► Atuadores
```

| Cliente | Publica | Consome |
|---------|---------|---------|
| **Sensor** | `SensorEvent` | — |
| **Decisor** | `HighLevelOrderEvent` | `SensorEvent` |
| **Atuador** | — | `HighLevelOrderEvent` |

### Tipos de cliente e áreas

- **Tipos** (enum `Type`): `SENSOR`, `DECIDER`, `ACTUATOR`
- **Áreas** (enum `Areas`): `LIVINGROOM`, `BEDROOM`, `KITCHEN`, `WC`, `GARDEN`

### Hierarquia de eventos

```
Event (abstrata)
├── RegistrationEvent                  # registo inicial do cliente
├── SensorEvent
│   ├── GroundMoistureSensorEvent
│   ├── HumiditySensorEvent
│   ├── LightSensorEvent
│   └── TemperatureSensorEvent
├── HighLevelOrderEvent
│   ├── AreaIrrigationOrderEvent
│   ├── AreaLightingOrderEvent
│   ├── AreaTemperatureOrderEvent
│   └── AreaVentilationOrderEvent
└── ReceptionConfirmationMessage       # (extra) confirmação de execução
```

---

## Funcionamento

### Servidor
- Cada cliente tem uma **thread dedicada** no servidor, que gere o envio e a receção de eventos.
- Atribui a cada cliente um **identificador de dispositivo único** em todo o sistema.
- **Recusa a ligação** se o tipo de cliente não existir.
- Publica os eventos nos tópicos correspondentes e gere as filas de cada tópico.

### Ciclo de vida de um evento
1. **Publicação** — o evento entra na fila do seu tópico e só é consumido quando chega à cabeça da fila.
2. **Consumo** — o servidor regista, com um `ModifiedCountdownLatch`, que clientes já consultaram o evento.
3. **Remoção** — o evento sai da fila quando todos os clientes subscritos confirmarem o consumo, ou após um tempo de espera pré-definido.
4. **Próximo evento** — o servidor avança para o evento seguinte do tópico. Se a fila esgotar, os atuadores esperam até haver novo evento.

### Mecanismos de coordenação
Todos os mecanismos de coordenação são **desenvolvidos pelo grupo**, sem usar os equivalentes da biblioteca padrão do Java.

```java
public class ModifiedCountdownLatch {
    public ModifiedCountdownLatch(int count);
    public synchronized void countdown();
    public synchronized void await(int waitPeriod) throws InterruptedException;
}
```

### Clientes
No arranque, o cliente liga-se automaticamente ao servidor e indica o seu tipo e a área da casa onde opera. Os atuadores subscrevem o tópico e a área adequados ao(s) dispositivo(s) que controlam.

### Automações demonstradas na GUI
- **Luz baixa** → o decisor ordena ligar as luzes e espera que todos os atuadores de iluminação acendam a lâmpada.
- **Ar húmido** → o decisor de ventilação emite uma ordem; os atuadores ligam ventiladores ou o ar condicionado em modo ventilação, **apenas se não estiver já em aquecimento ou arrefecimento**.
- **Solo seco** → o decisor de rega emite uma ordem de irrigação; os atuadores ligam a irrigação do jardim.
- **Temperatura acima do limite** → liga-se o ar condicionado, para aquecer ou arrefecer.

### Histórico de ações
Cada atuador mantém um histórico, consultável pelo utilizador, de todas as ações tomadas desde que foi ligado. Exemplo: `Lâmpada desligada - HH:MM:SS`.

### Extra: confirmações de execução
Após processar uma ordem, o atuador pode enviar um `ReceptionConfirmationMessage`. O servidor aguarda todas as confirmações de um tópico antes de registar o sucesso da interação.

```json
{
  "event": "evento-recebido",
  "deviceId": "id-do-cliente-atuador",
  "status": "OK" // ou "NOK"
}
```

---

## Estrutura do repositório

```
IscoT_PCD/
├── src/iscot/     # Código-fonte (servidor, clientes, eventos, GUI)
├── .gitignore
└── README.md
```

## Requisitos

- Java (JDK) <!-- indicar a versão usada -->

## Compilar e executar

> Substituir `<Classe>` pelas classes `main` reais do projeto.

```bash
# Compilar
javac -d bin src/iscot/*.java

# Servidor
java -cp bin iscot.<ClasseServidor> <PORT>

# Cliente (sensor, decisor ou atuador)
java -cp bin iscot.<ClasseCliente> <IP> <PORT> <AreaDispositivo> <TipoDispositivo>
```

Exemplo de cliente:

```bash
java -cp bin iscot.<ClasseCliente> localhost 8080 KITCHEN ACTUATOR
```

---

## Fases de desenvolvimento

- [x] **Fase 1** — GUI: componentes gráficos dos sensores e atuadores
- [x] ~~Fase 2~~ — suprimida (já resolvida no código base através dos enums `Areas` e `Type`)
- [x] **Fase 3** — Representação de eventos nas janelas da GUI (eventos de exemplo nos atuadores; eventos gerados pelos sensores apresentados na consola)
- [x] **Fase 4** — Servidor e ligação inicial dos clientes (modelo cliente-servidor)
- [ ] **Fase 5** — Troca de eventos entre clientes e servidor (canais de objetos)
- [ ] **Fase 6** — Troca simples de eventos (1 sensor, 1 decisor, 1 atuador)
- [ ] **Fase 7** — Consulta dos eventos com `ModifiedCountdownLatch` por tópico
- [ ] **Fase 8 (extra)** — Consumo confirmado dos eventos de alto nível

## Autores

- <!-- Nome 1 — nº de aluno -->
- <!-- Nome 2 — nº de aluno -->

Unidade curricular: Programação Concorrente e Distribuída (PCD) — ISCTE, 2026/27