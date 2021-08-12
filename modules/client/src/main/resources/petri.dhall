let RPC :Type = {name: String, input: String, output: String}
let Service:Type  = {packageName: String = "", name: String = "", rpcs: List[RPC]}
let LinkableElement = <Place: {id: Int, name: String, capacity: Int}| Circle: {id: NodeId, name: String, service:Service, rpc:RPC}>