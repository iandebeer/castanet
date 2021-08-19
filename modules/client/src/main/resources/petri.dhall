let RPC : Type = {name : Text, input : Text,  output : Text}
let Service : Type = {packageName : Text, name : Text, rpcs: List RPC} 
let ProtoItem = <R : RPC | S : Service>

let Place: Type = {id : Natural, name : Text, capacity : Natural}
let Transition : Type = {id : Natural, name : Text , service : Service, rpc : RPC}

let LinkableElement = < P: Place | T : Transition >

let Timed : Type = {from: Natural, to: Natural, interval: Natural}
  
let Weighted : Type = {from: Natural, to: Natural, weight: Natural}

let Arc = <T: Timed | W: Weighted>

let PetriElement : Type = < A : Arc | L : LinkableElement >

let PetriNet : Type = List PetriElement 

let places =  ./places.dhall

let transitions  = ./transitions.dhall

let List/map = https://prelude.dhall-lang.org/v11.1.0/List/map sha256:dd845ffb4568d40327f2a817eb42d1c6138b929ca758d50bc33112ef3c885680
let linkablePlace = List/map Place LinkableElement (\(p : Place) -> LinkableElement.P p)
let linkableTransition = List/map Transition LinkableElement (\(t : Transition) -> LinkableElement.T t)

in  [linkablePlace places, linkableTransition transitions]