package IllegalRequestParameterException

class IllegalParameterException(private val parameter: String?) : Throwable("Illegal parameter $parameter"){
    override val message: String
        get() = "Illegal parameter $parameter"
}
