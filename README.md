# Castanet is a Colored Petri Net for GRPC/HTTP orchestration (e.g. FS2-GRPC or HTTP4S)

Formally, a Petri Net is a state transition graph that maps Places (circles) to Transitions (rectangles) and Transitions to Places via Arcs (arrows).
It is well suited for describing the flow of concurrent processes.

From the Castanet perspective, Petri Nets are directed graphs consisting of Places(States), Transitions(Services) and Arcs(Guards). It models state-transitions of (concurrent) processes.
It is easy to see (if you are that way inclined) that Petri Nets form a Category of Petri  
Protobuf definitions specify the service and message format for GRPC services.
An GRPC/HTTP call is assumed to be stateless, yet often there is a need to have service calls handled within the context of a state machine (FSM)

Castanet constructs a PetriNet using a builder-pattern

```scala
val p1 = Place(1, "start", 1)
val p2 = Place(2, "left", 3)
val p3 = Place(3, "right", 1)
val p4 = Place(4, "joint", 3)
val p5 = Place(5, "end", 1)

val t1 = Transition(6, "splitter", (l: LinkableElement) => println(l))
val t2 = Transition(7, "joiner", (l: LinkableElement) => println(l))
val t3 = Transition(8, "continuer", (l: LinkableElement) => println(l))

val b1 = PetriNetBuilder().addAll(ListSet(p1, p2, p3, p4, p5))
val b2 = n.addAll(ListSet(t1, t2, t3))
val b3 = n2
  .add(Weighted(1, 6, 1))
  .add(Weighted(6, 2, 1))
  .add(Weighted(6, 3, 1))
  .add(Weighted(2, 7, 2))
  .add(Weighted(3, 7, 1))
  .add(Weighted(7, 4, 1))
  .add(Weighted(4, 8, 3))
  .add(Weighted(8, 5, 1))
val petrinet = b3.build()
```

State is attributed to the Petri Net through Markers that associate a BitVectors(scodec.bits) with a specific Place

```scala
val m1 = Markers(pn)
val m2 = m1.setMarker(Marker(1, bin"1"))
val m3 = m2.setMarker(Marker(2, bin"1")).setMarker(Marker(4, bin"11"))
```

![alt text](modules/core/src/test/resource/petrinet1.png "Petri Net 1")

For a given set of Markers (current state) the PetriNet can be asked to step through to the next state (set of markers) as indicated by the guards placed on the Arcs that join Places and Transitions.

A ColouredPetrNet is traversable using a state monad to step from an initial state

The resulting state changes can be visualized with a PetriPrinter.

```scala
    PetriPrinter(fileName = "petrinet1", petriNet = pn).print(Option(m3))
    val steps: State[Step, Unit] =
      for
        p1 <- pn.step
        p2 <- pn.step
        p3 <- pn.step
      yield (
        PetriPrinter(fileName = "petrinet2", petriNet = pn).print(Option(p1)),
        PetriPrinter(fileName = "petrinet3", petriNet = pn).print(Option(p2)),
        PetriPrinter(fileName = "petrinet4", petriNet = pn).print(Option(p3))
      )
    steps.run(Step(m3, true, 1)).value
```

![alt text](modules/core/src/test/resource/animate.gif "Petri Net Animation")

We derive Transitions from ProtoBuf files that indicates the RPC's we use in the business flow.
The Transitions are described using a Dhall format list:

```dhall
[
  {
      id = 2 
    , name = "testTransition"
    , service = {
          packageName = "packageName1"
        , name = "serviceName1"   
        , rpcs = [
          {name = "rpc1"
          , input = "in1"
          , output = "out1"
          }
        ]  
    }
    , rpc = {name = "rpc1"
          , input = "in1"
          , output = "out1"
          }
  }
]
```

Transitions change the States of the Workflow as described by a list of Places:

```dhall
  [{
    id = 1 
    , name = "place1"
    , capacity = 2
  }]
  ```

A business analyst can join the Places (States) and Transitions by drawing Arcs 

![alt text](docs/place_transitions.png "Arcs")

