
import ee.mn8.castanet.Dag
val zzz = """{
  "tasks" : [
    {
      "name" : "A",
      "template" : "echo",
      "arguments" : {
        "parameters" : [
          {
            "name" : "message",
            "value" : "A"
          }
        ]
      }
    },
    {
      "name" : "B",
      "dependencies" : [
        "A"
      ],
      "template" : "echo",
      "arguments" : {
        "parameters" : [
          {
            "name" : "message",
            "value" : "B"
          }
        ]
      }
    },
    {
      "name" : "C",
      "dependencies" : [
        "A"
      ],
      "template" : "echo",
      "arguments" : {
        "parameters" : [
          {
            "name" : "message",
            "value" : "C"
          }
        ]
      }
    },
    {
      "name" : "D",
      "dependencies" : [
        "B",
        "C"
      ],
      "template" : "echo",
      "arguments" : {
        "parameters" : [
          {
            "name" : "message",
            "value" : "D"
          }
        ]
      }
    }
  ]
}"""

import io.circe._, io.circe.parser._
import io.circe.generic.auto._, io.circe.syntax._

decode[Dag](zzz)

