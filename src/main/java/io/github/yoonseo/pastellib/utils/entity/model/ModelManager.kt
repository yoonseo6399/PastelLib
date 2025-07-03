package io.github.yoonseo.pastellib.utils.entity.model

import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import org.bukkit.entity.Display

/**
 * 모든 Model 인스턴스의 생명주기를 관리하는 중앙 관리자 클래스입니다.
 * 모델의 주기적인 업데이트, 자동 정리, 그리고 안전한 일괄 제거를 담당합니다.
 */
object ModelManager {

    // 모든 활성화된 모델을 저장하는 리스트. Model<*>를 통해 모든 타입의 Display 모델을 저장할 수 있습니다.
    private val models = mutableListOf<Model<*>>()

    // 주기적으로 모델을 업데이트하는 메인 태스크
    private lateinit var updateTask: Promise

    /**
     * ModelManager가 생성될 때, 모델을 매 틱마다 업데이트하고 정리하는 태스크를 시작합니다.
     */
    init {
        startUpdateTask()
    }

    private fun startUpdateTask() {
        updateTask = syncRepeating(20) { // 매 초마다 실행
            models.removeIf { it.isDead }
        }
    }

    /**
     * Model의 팩토리 메소드에 의해서만 호출되는 내부용 메소드입니다.
     * 생성된 모델을 관리 리스트에 추가합니다.
     * @param model 관리할 Model 인스턴스
     */
    internal fun addModel(model: Model<*>) {
        models.add(model)
    }

    /**
     * 특정 모델을 관리 리스트에서 제거합니다.
     * @param model 제거할 Model 인스턴스
     * @return 리스트에서 성공적으로 제거되면 true
     */
    internal fun removeModel(model: Model<*>) : Boolean {
        return models.remove(model)
    }

    /**
     * 플러그인이 비활성화될 때 호출되어야 하는 메소드입니다.
     * 모든 관리 중인 모델을 안전하게 제거하고 태스크를 종료합니다.
     */
    fun shutdown() {
        // 1. 새로운 모델이 추가되거나 제거되는 것을 막기 위해 업데이트 태스크부터 중지
        if (this::updateTask.isInitialized) {
            updateTask.cancel()
        }

        // 2. ConcurrentModificationException을 피하기 위해 리스트 복사본을 순회하며 모든 모델 제거
        models.toList().forEach { model ->
            model.remove()
        }

        // 3. 모든 모델이 제거된 후, 메인 리스트를 완전히 비웁니다.
        models.clear()
        println("ModelManager가 모든 모델을 안전하게 제거하고 종료되었습니다.")
    }
}
