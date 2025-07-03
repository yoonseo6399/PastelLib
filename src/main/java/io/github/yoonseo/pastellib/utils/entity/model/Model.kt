package io.github.yoonseo.pastellib.utils.entity.model
import io.github.yoonseo.pastellib.utils.entity.blockDisplays.AdvancedBlockDisplay
import io.github.yoonseo.pastellib.utils.tasks.Promise
import io.github.yoonseo.pastellib.utils.tasks.syncRepeating
import io.papermc.paper.entity.TeleportFlag
import org.bukkit.*
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Display
import org.joml.*
import kotlin.reflect.KClass


typealias DisplayModel = Model<BlockDisplay>




class DefaultModel<T : Display>(name : String) : Model<T>(name){
    override val renderer = ModelRenderer(this)
}




abstract class Model<T : Display> protected constructor(val name: String){
    companion object{
        /**
         * @param initializer called before model initialized, used for constructor's param injection or initialize
         * @param parameter constructor's param **/
        fun <T : Display,M : Model<T>> spawn(location: Location,modelClazz: KClass<M>,initializer : (M.() -> Unit)? = null,vararg parameter : Any?) : M{
            val model = modelClazz.constructors.find { constructor ->
                val constructorParams = constructor.parameters
                if (constructorParams.size != parameter.size) return@find false
                constructorParams.zip(parameter).all { (ctorParam, arg) ->
                    val ctorType = ctorParam.type.classifier as? kotlin.reflect.KClass<*>
                    when {
                        arg == null -> ctorParam.type.isMarkedNullable
                        ctorType != null -> ctorType.isInstance(arg)
                        else -> false
                    }
                }
            }?.call(*parameter)
                ?: throw IllegalArgumentException("Cannot find matching constructor for ${modelClazz.simpleName} with parameters ${parameter.map { it?.javaClass?.name ?: "null" }}")
            val res: RenderResult<T> = model.renderer.load(location)
            initializer?.let { model.it() }
            model.initialize(location,res)
            ModelManager.addModel(model)
            return model
        }
        fun <T : Display,M : Model<T>> spawn(location: Location,modelClazz : KClass<M>,vararg parameter : Any?) = spawn(location,modelClazz,null,*parameter)
        //var constructorFailureMessage = ""
    }
    open fun initialize(location: Location,renderResult: RenderResult<T>){
        this._mainDisplay = renderResult.mainDisplay
        this.displayData = renderResult.displayDatas
    }
    abstract val renderer : ModelRenderer<T>
    private lateinit var _mainDisplay: T
    val mainDisplay: T
        get() {
            // 초기화 전에 접근 시도 시 명확한 에러 메시지 제공 (선택 사항이지만 추천)
            if (!this::_mainDisplay.isInitialized) {
                throw IllegalStateException("Model이 아직 완전히 초기화되지 않았습니다. 팩토리 메소드를 통해 생성해야 합니다.")
            }
            return _mainDisplay
        }
    lateinit var displayData: List<DisplayData>
    val location : Location
        get() = mainDisplay.location

    val isDead : Boolean
        get() = mainDisplay.isDead || mainDisplay.passengers.any { it.isDead }
    //val parts : List<ModelPart>
    val modules = mutableSetOf<ModelModule<T>>()
    val displays : MutableList<T>
        get() {
            require(validate()) { "Invalid Model structure" }
            @Suppress("UNCHECKED_CAST")
            return mainDisplay.passengers as MutableList<T>
        }

    fun attachModule(module : ModelModule<T>){
        modules.add(module)
        module.onAttach(this)
    }
    fun detachModule(module: ModelModule<T>) : Boolean{
        module.onDetach(this)
        return modules.remove(module)
    }

    open fun remove(){
        modules.forEach { it.onDetach(this) }
        for (passenger in mainDisplay.passengers) {
            passenger.remove()
        }
        mainDisplay.remove()
    }

    fun teleport(location: Location){
        mainDisplay.teleport(location, TeleportFlag.EntityState.RETAIN_PASSENGERS)
        mainDisplay.passengers.forEach {
            it.teleport(location,TeleportFlag.EntityState.RETAIN_VEHICLE)
        }
    }
    fun rotate(quaternionf: Quaternionf){
        AdvancedBlockDisplay.getBy(mainDisplay as BlockDisplay).rotate(quaternionf) // text Display 지원 안함 TODO
        mainDisplay.passengers.forEach { AdvancedBlockDisplay.getBy(it as BlockDisplay).rotate(quaternionf) }
    }
    fun applyGlobalRotation(quaternionf: Quaternionf){
        AdvancedBlockDisplay.getBy(mainDisplay as BlockDisplay).globalRotation(quaternionf)
        displays.forEach {
            AdvancedBlockDisplay.getBy(it as BlockDisplay).globalRotation(quaternionf)
        }
    }
    fun interpolation(){
        displays.forEach {
            it.interpolationDuration = 1
            it.interpolationDelay = -1
        }
    }


    fun validate() : Boolean {
        return mainDisplay.passengers.all { it is Display }
    }
}

class ValidationModule(val checkInterval : Int) : ModelModule<Display>() {
    lateinit var task : Promise
    override fun onAttach(model: Model<Display>) {
        task = syncRepeating(interval = checkInterval.toLong()) {
            if(model.validate() || model.isDead) model.remove()
        }
    }

    override fun onDetach(model: Model<Display>) {
        task.cancel()
    }
}
fun <T : Display,M : Model<T>>Location.spawnModel(modelClazz : KClass<M>, initializer : (M.() -> Unit)? = null, vararg parameter : Any?) : M =
    Model.spawn(this,modelClazz,initializer,*parameter)

fun <T : Display,M : Model<T>> Location.spawnModel(modelClazz : KClass<M>,vararg parameter : Any?) : M =
    spawnModel(modelClazz, null,*parameter)

