package com.tapbi.spark.yokey.util

import timber.log.Timber

class MyDebugTree : Timber.DebugTree() {
    //    override fun createStackElementTag(element: StackTraceElement): String {
//        return String.format(
//            "(%s:%s)#%s",
//            element.fileName,
//            element.lineNumber,
//            element.methodName
//        )
//    }
//    @Override
//    protected String createStackElementTag(StackTraceElement element) {
//        return String.format("(%s:%s)#%s",
//                element.getFileName(),
//                element.getLineNumber(),
//                element.getMethodName());
//    }
    var fileName: String? = null

    override fun createStackElementTag(element: StackTraceElement): String {
        fileName = element.fileName
        return String.format(
            "(%s:%s)#%s",
            element.fileName,
            element.lineNumber,
            element.methodName
        )
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val mTag = fileName!!
        val mMessage = "$tag $message"
        super.log(priority, mTag, mMessage, t)
    }
}