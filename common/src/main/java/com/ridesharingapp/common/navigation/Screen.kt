package com.ridesharingapp.common.navigation

import androidx.navigation.NamedNavArgument

abstract class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = listOf()
) {
    val routeWithArgs = buildString {
        append(route)
        arguments.forEachIndexed { index, arg ->
            append("${if (index == 0) "?" else "&"}${arg.name}={${arg.name}}")
        }
    }
    init {
        println(routeWithArgs)
    }

    fun withArgs(vararg args: String?): String = buildString {
        append(route)
//        var hasInitialArg = false
        args.forEachIndexed { index, arg ->
//            if (arg != null) {
            append("${if (index == 0) "?" else "&"}${arguments[index].name}=$arg")
//                hasInitialArg = true
//            }
        }
    }
}