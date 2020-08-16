package com.xiangzi.video.utils;

import android.annotation.SuppressLint;
import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 全局反射Application类
 */
public class XzAppGlobals {

    private static Application _application;

    /**
     * 反射获取当前的Application 上下文
     * @return application
     */
    @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
    public static Application get(){
        if(_application == null){

            try {
                Method method = Class.forName("android.app.ActivityThread").
                        getDeclaredMethod("currentApplication");
                try {
                    _application = (Application) method.invoke(null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }catch (Error e){
                    e.printStackTrace();
                }catch (Throwable e){
                    e.printStackTrace();
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }catch (Error e){
                e.printStackTrace();
            }catch (Throwable e){
                e.printStackTrace();
            }

        }
        return _application;
    }

}
