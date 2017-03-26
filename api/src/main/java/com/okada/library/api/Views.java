package com.okada.library.api;


import android.app.Activity;

import java.lang.reflect.Method;

public class Views {

    private Views() {
        // No instances.
    }

    public static void inject(Activity activity) {
        try {
            Class<?> injector = Class.forName(activity.getClass().getName() + "$$ViewInjector");
            Method inject = injector.getMethod("inject", activity.getClass());
            inject.invoke(null, activity);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject views for activity " + activity, e);
        }
    }
}
