let RPC : Type = {
  name : Text
  , input : Text
  ,  output : Text
  }
let Service : Type = {
  packageName : Text 
  , name : Text
  , rpcs: List RPC
  } 
let ProtoItem : Type = <RPC | Service>

let Place = {
  id : Natural
  , name : Text
  , capacity : Natural
  }
let Transition = {
  id : Natural 
  , name : Text 
  , service : Service
  , rpc : RPC
  }
let LinkableElement : Type = <Place | Transition>

let Timed : Type = {
  from: Natural
  , to: Natural
  , interval: Natural
}
  
let Weighted : Type = {
  from: Natural
  , to: Natural
  , weight: Natural
}

let Arc : Type = <Timed | Weighted>

let PetriElement : Type = <Arc | LinkableElemnet>

let places: List Place = ./places.dhall
-- in [places, transitions, arcs]
in places