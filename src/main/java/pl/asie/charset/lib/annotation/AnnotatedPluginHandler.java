package pl.asie.charset.lib.annotation;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

public abstract class AnnotatedPluginHandler<T> {
    private final Class annotationClass;
    private Set<T> plugins = null;

    protected AnnotatedPluginHandler(Class annotationClass) {
        this.annotationClass = annotationClass;
    }

    @SuppressWarnings("unchecked")
    public Set<T> getPlugins() {
        if (plugins == null) {
            ImmutableSet.Builder<T> builder = new ImmutableSet.Builder<>();

            for (String s : AnnotationHandler.classNames.get(annotationClass)) {
                try {
                    T plugin = (T) Class.forName(s).newInstance();
                    builder.add(plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            plugins = builder.build();
            AnnotationHandler.classNames.removeAll(annotationClass);
        }
        return plugins;
    }

}
