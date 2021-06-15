Castanet is a Colored Petri Net for GRPC orchestration using Cats, FS2 and FS2-GRPC

Protobuf definitions specify the service and message format for GRPC services.
GRPC call are assumed to be stateless, yet often there is a need to have GRPC calls handled within the context of a state machine (FSM)
