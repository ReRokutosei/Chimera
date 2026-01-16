# Proguard rules for embedded-picker-library
# Keep public classes and methods
-keep public class com.t8rin.embeddedpicker.** {
    public *;
}

# Keep annotations
-keepattributes *Annotation*

# Keep parameter names
-keepparameternames