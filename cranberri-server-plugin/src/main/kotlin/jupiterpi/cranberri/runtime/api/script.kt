package jupiterpi.cranberri.runtime.api

@Target(AnnotationTarget.CLASS)
annotation class Script(val name: String)

@Target(AnnotationTarget.FUNCTION)
annotation class Setup

@Target(AnnotationTarget.FUNCTION)
annotation class Tick