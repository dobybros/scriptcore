#
# This ProGuard configuration file illustrates how to process servlets.
# Usage:
#     java -jar proguard.jar @servlets.pro
#

# Specify the input jars, output jars, and library jars.

-injars  deploy/war/WEB-INF/lib/a.jar
-outjars deploy/war/WEB-INF/lib/ao.jar

-libraryjars <java.home>/lib/rt.jar
-libraryjars deploy/war/WEB-INF/lib

-include deploy/war/WEB-INF/lib/annotations.pro

# Save the obfuscation mapping to a file, so you can de-obfuscate any stack
# traces later on. Keep a fixed source file attribute and all line number
# tables to get line numbers in the stack traces.
# You can comment this out if you're not interested in stack traces.

-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
-verbose 
-dontoptimize
#-allowaccessmodification
-dontusemixedcaseclassnames
#-defaultpackage a 
-dontshrink
-dontwarn
#-printseeds obfuscateseeds.txt 
#-printusage obfuscateusage.txt 
-printmapping obfuscatemapping.txt

# Preserve all annotations.

-keepattributes *Annotation*

# You can print out the seeds that are matching the keep options below.

#-printseeds out.seeds

# Preserve all public servlets.

-keep public class * implements javax.servlet.Servlet

# Preserve all native method names and the names of their classes.

-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Preserve the special static methods that are required in all enumeration
# classes.

-keepclassmembers,allowoptimization enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Your application may contain more items that need to be preserved;
# typically classes that are dynamically created using Class.forName:

# -keep public class mypackage.MyClass
# -keep public interface mypackage.MyInterface
# -keep public class * implements mypackage.MyInterface


-keepclassmembers class * {
    @javax.annotation.Resource *;
}

-keep public class chat.main.ServerStart {
	public *;
}
-keep public class com.acucore.utils.HttpSessionCollector {
	public *;
}
-keep public class com.talentaccounts.intercepters.AuthenticationIntercepter {
	public *;
}
-keep public class com.talentcore.intercepters.CommonIntercepter {
	public *;
}

-keep @org.springframework.stereotype.Controller public class *
-keep @org.springframework.stereotype.Service public class *


#-keep class * {
#	void set*(***);
#	*** get*();
#	boolean is*();
#}