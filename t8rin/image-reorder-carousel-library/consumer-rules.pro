# Proguard rules for image-reorder-carousel-library
# Keep public classes and methods
-keep public class com.t8rin.imagereordercarousel.** {
    public *;
}

# Keep annotations
-keepattributes *Annotation*

# Keep parameter names
-keepparameternames