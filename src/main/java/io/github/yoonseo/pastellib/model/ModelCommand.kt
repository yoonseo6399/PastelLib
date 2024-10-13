package io.github.yoonseo.pastellib.model

import io.github.monun.kommand.kommand
import io.github.yoonseo.pastellib.PastelLib

class ModelCommand {
    init {
        PastelLib.instance.kommand {
            register("pastelLib"){
                then("model"){
                    then("loadFromWorld"){
                        executes {  }
                    }
                }
            }
        }
    }
}