package com.facebook.galleryapp

class NoDuplicateStack {
    val stack: MutableList<Any> = mutableListOf()
    val size: Int
        get() = stack.size

    // Push element onto the stack
    fun push(p: Any) {
        val index = stack.indexOf(p)
        if (index != -1) {
            stack.removeAt(index)
        }
        stack.add(p)
    }

    // Pop upper element of stack
    fun pop(): Any? {
        return if (size > 0) {
            stack.removeAt(stack.size - 1)
        } else {
            null
        }
    }

    // Look at upper element of stack, don't pop it
    fun peek(): Any? {
        return if (size > 0) {
            stack[stack.size - 1]
        } else {
            null
        }
    }
}

