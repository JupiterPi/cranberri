package jupiterpi.cranberri.runtime.api

@Target(AnnotationTarget.CLASS)
annotation class Script

@Target(AnnotationTarget.FUNCTION)
annotation class Setup

@Target(AnnotationTarget.FUNCTION)
annotation class Tick

@Target(AnnotationTarget.FUNCTION)
annotation class Loop