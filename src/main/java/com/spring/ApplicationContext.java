package com.spring;

import com.spring.Interfaces.BeanFactory;
import com.spring.components.AOP.*;
import com.spring.Annotations.*;
import com.spring.Interfaces.BeanNameAware;
import com.spring.Interfaces.BeanPostProcessor;
import com.spring.Interfaces.InitializingBean;
import com.spring.constant.Context;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring框架容器类
 * 核心功能就两条，我们需要能够根据配置类进行文件扫描，并且根据扫描结果创建各个类并作为Bean保管，并且可以通过getBean()方法获取
 */
public class ApplicationContext implements Context {
    private static Class configClass;
    // 单例池，一级缓存
    private static final ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, BeanFactory> singletonFactories = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    // AOP三大核心，PostProcessor列表
    private static final List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();
    // AOP处理方法列表
    private static final MethodBeforeAdviceList methodBeforeAdviceList;
    private static final AfterReturningAdviceList afterReturningAdviceList;

    // 初始化容器，加载必要组件
    static {
        ClassLoader classLoader = ApplicationContext.class.getClassLoader();

        // 初始化前后置处理器列表并直接加入容器，之所以要在这里处理，是因为我们的advice会在扫描组建时加入，而这两个组件必须更早加入
        methodBeforeAdviceList = new MethodBeforeAdviceList();
        afterReturningAdviceList = new AfterReturningAdviceList();
        BeanDefinition methodBeforeAdviceListDefinition = new BeanDefinition(MethodBeforeAdviceList.class, Scope.SCOPE_SINGLETON, false);
        BeanDefinition afterReturningAdviceListDefinition = new BeanDefinition(AfterReturningAdviceList.class, Scope.SCOPE_SINGLETON, false);
        getBeanDefinitionMap().put(BEFORE_ADVICE_LIST, methodBeforeAdviceListDefinition);
        getBeanDefinitionMap().put(AFTER_ADVICE_LIST, afterReturningAdviceListDefinition);
        singletonObjects.put(BEFORE_ADVICE_LIST, methodBeforeAdviceList);
        singletonObjects.put(AFTER_ADVICE_LIST, afterReturningAdviceList);

        doScan(COMPONENT_PATH, classLoader);
    }
    private static ConcurrentHashMap<String, BeanDefinition> getBeanDefinitionMap(){
        return beanDefinitionMap;
    }
    private static List<BeanPostProcessor> getPostProcessorList(){
        return beanPostProcessorList;
    }

    private static MethodBeforeAdviceList getMethodBeforeAdviceList(){
        return methodBeforeAdviceList;
    }
    private static AfterReturningAdviceList getAfterReturningAdviceList(){
        return afterReturningAdviceList;
    }

    private static boolean isPostProcessable(Class clazz){
        return !BeanPostProcessor.class.isAssignableFrom(clazz) &&
                !MethodBeforeAdvice.class.isAssignableFrom(clazz) &&
                !AfterReturningAdvice.class.isAssignableFrom(clazz) &&
                !clazz.equals(MethodBeforeAdviceList.class) &&
                !clazz.equals(AfterReturningAdviceList.class);
    }

    public ApplicationContext(Class configClass){
        this.configClass = configClass;

        // 接受配置类，接下有这么几步，解析 ---> 获取扫描路径 ---> 扫描

        scan(this.configClass);

        for(Map.Entry<String, BeanDefinition> entry:getBeanDefinitionMap().entrySet()){
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = getBeanDefinitionMap().get(beanName);
            if(beanDefinition.getScope() == Scope.SCOPE_SINGLETON){
                Object bean = createBean(beanName, beanDefinition);
                // System.out.println("put bean " + bean.getClass().getSimpleName() + " into singleton pool!");
                singletonObjects.put(beanName, bean);
            }
        }
    }

    public static Object createBean(String beanName, BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getClazz();
        try {

            // 此处可以改进，给予构造器选择，优先选用@Autowired的构造方法。这些构造方法如果有入参，必须得是Spring容器中的对象，否则报错
            // 那么怎么找这些参数呢？总结起来就是先Type后name，其中除非class唯一，否则name必须完全匹配
            Object bean = clazz.getDeclaredConstructor().newInstance();

            singletonFactories.put(beanName, () -> getEarlyBeanReference(beanName, beanDefinition, bean));
            Object instance = bean;

            // 依赖注入，处理Bean中的其他自动配置项
            doDependencyInject(instance);


            // NameAware回调，可以获取Bean在容器的注册名
            if(instance instanceof BeanNameAware){
                ((BeanNameAware) instance).setBeanName(beanName);
            }

            // BeanPostProcessor处理，注意这里BeanPostProcessor本身也是Bean，需要排除以避免死循环
            if(isPostProcessable(clazz)){
                for (BeanPostProcessor postProcessor : getPostProcessorList()) {
                    instance = postProcessor.postProcessBeforeInitialization(instance, beanName);
                }
            }

            // Bean的初始化，分两步（真正的Spring中是三步，这里少了最后一步即xml配置的init-method，因为没做）
            // 首先是@PostConstruct注解的方法，其次是InitializingBean接口的方法
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for(Method method:declaredMethods){
                if(method.isAnnotationPresent(PostConstruct.class)){
                    method.invoke(instance);
                }
            }
            if(instance instanceof InitializingBean){
                try {
                    ((InitializingBean) instance).afterPropertiesSet();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            // BeanPostProcessor处理，AOP核心，这里是初始化后处理，注意这里BeanPostProcessor本身也是Bean，需要排除以避免死循环
            if(isPostProcessable(clazz)) {
                for (BeanPostProcessor postProcessor : getPostProcessorList()) {
                    if(postProcessor.getClass().isAnnotationPresent(Synthetic.class) && !beanDefinition.isSynthetic()){
                        continue;
                    }
                    instance = postProcessor.postProcessAfterInitialization(instance, beanName);
                }
            }

            if(singletonFactories.containsKey(beanName)){
                singletonFactories.remove(beanName);
            }
            if(earlySingletonObjects.containsKey(beanName)) {
                instance = earlySingletonObjects.remove(beanName);
            }
            return instance;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static void scan(Class configClass) {
        // 解析配置类
        ComponentScan annotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String configPath = annotation.value().replace(".", "/");
        // 扫描

        ClassLoader classLoader = ApplicationContext.class.getClassLoader();

        doScan(configPath, classLoader);
    }
    private static void doScan(String path, ClassLoader classLoader){
        // 这里的相对路径走的是相对于 classpath 的路径
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        doScan(file, classLoader);
    }

    private static void doScan(File file, ClassLoader classLoader){
        if(file.isDirectory()){
            File[] files = file.listFiles();

            for(File f:files){
                doScan(f, classLoader);
            }
        }else if(file.isFile()){
            String absolutePath = file.getAbsolutePath();

            if(absolutePath.endsWith(".class")) {

                String className = absolutePath.substring(absolutePath.indexOf("com"), absolutePath.indexOf(".class"));
                className = className.replace("\\", ".");

                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    if (clazz.isAnnotationPresent(Component.class)) {
                        // 这就表示，这个类是一个Bean，需要创建出来，注入容器
                        // System.out.println(clazz.getSimpleName());

                        // AOP三核心，后两个是切面方法，这些组件需要提前加载进入容器，所以在BeanDefinition阶段就读取
                        if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                            BeanPostProcessor postProcessor = (BeanPostProcessor) clazz.getDeclaredConstructor().newInstance();
                            doDependencyInject(postProcessor);
                            getPostProcessorList().add(postProcessor);
                        }
                        if(MethodBeforeAdvice.class.isAssignableFrom(clazz)){
                            MethodBeforeAdvice methodBeforeAdvice = (MethodBeforeAdvice) clazz.getDeclaredConstructor().newInstance();
                            doDependencyInject(methodBeforeAdvice);
                            getMethodBeforeAdviceList().add(methodBeforeAdvice);
                        }
                        if(AfterReturningAdvice.class.isAssignableFrom(clazz)){
                            AfterReturningAdvice afterReturningAdvice = (AfterReturningAdvice) clazz.getDeclaredConstructor().newInstance();
                            doDependencyInject(afterReturningAdvice);
                            getAfterReturningAdviceList().add(afterReturningAdvice);
                        }

                        // 要创建一个Bean，首先需要判断当前Bean是单例，还是原型
                        // 并且当我们获取Bean时同样要进行此操作，这样就非常复杂，所以我们需要BeanDefinition，这就是scan()方法的意义
                        // 解析后，就得生成一个BeanDefinition对象
                        // 此处存在懒加载可以改进
                        Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                        String beanName = componentAnnotation.value();

                        BeanDefinition beanDefinition = new BeanDefinition();
                        beanDefinition.setClazz(clazz);

                        if (clazz.isAnnotationPresent(Scope.class)) {
                            Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                            beanDefinition.setScope(scopeAnnotation.value());
                        } else {
                            beanDefinition.setScope(Scope.SCOPE_SINGLETON);
                        }

                        if(clazz.isAnnotationPresent(Synthetic.class)){
                            Synthetic syntheticAnnotation = clazz.getDeclaredAnnotation(Synthetic.class);
                            beanDefinition.setSynthetic(Synthetic.NOT_SYNTHETIC);
                        }else{
                            beanDefinition.setSynthetic(Synthetic.SYNTHETIC);
                        }

                        getBeanDefinitionMap().put(beanName, beanDefinition);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                } catch (InstantiationException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    // 依赖注入，处理Bean中的其他自动配置项
    public static void doDependencyInject(Object instance){
        try {
            for (Field declaredField : instance.getClass().getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Autowired autowired = declaredField.getDeclaredAnnotation(Autowired.class);
                    Object bean = getBean(declaredField.getName());
                    if (bean == null && autowired.required()) {
                        throw new RuntimeException("Could not find necessary Bean in container! initial Bean failed!");
                    }
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Object getBean(String beanName){
        if(getBeanDefinitionMap().containsKey(beanName)){
            BeanDefinition beanDefinition = getBeanDefinitionMap().get(beanName);

            if(beanDefinition.getScope() == Scope.SCOPE_SINGLETON) {
                Object bean = getSingleton(beanName);
                if (bean == null) {
                    bean = createBean(beanName, beanDefinition);
                }
                return bean;
            }else{
                return createBean(beanName, beanDefinition);
            }
        }else{
            throw new NullPointerException(beanName + " not found in container!");
        }
    }

    public static Object getSingleton(String beanName){
        Object singletonObject = singletonObjects.get(beanName);
        if(singletonObject == null){
            singletonObject = earlySingletonObjects.get(beanName);
            if(singletonObject == null){
                BeanFactory singletonFactory = singletonFactories.get(beanName);
                if(singletonFactory != null){
                    // 这里的getObject就是下面的getEarlyBeanReference()
                    singletonObject = singletonFactory.getObject();
                    earlySingletonObjects.put(beanName, singletonObject);
                    singletonFactories.remove(beanName);
                }
            }
        }
        return singletonObject;
    }

    // 用于获取Bean的早期对象，在存在循环依赖时，我们需要在此处理AOP
    public static Object getEarlyBeanReference(String beanName, BeanDefinition beanDefinition, Object bean){
        Object singletonObject = bean;
        if(beanDefinition.isSynthetic()){
            for (BeanPostProcessor beanPostProcessor: beanPostProcessorList){
                singletonObject = beanPostProcessor.postProcessBeforeInitialization(singletonObject, beanName);
            }
            for (BeanPostProcessor beanPostProcessor: beanPostProcessorList){
                singletonObject = beanPostProcessor.postProcessAfterInitialization(singletonObject, beanName);
            }
        }
        beanDefinition.setSynthetic(Synthetic.NOT_SYNTHETIC);
        return singletonObject;
    }
}
