import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class CustomThreadGroup extends ThreadGroup {

    private static final long serialVersionUID = 1L;

    public CustomThreadGroup(ThreadGroup parent, String name) {
        super(createProxy(parent), name);
    }

    private static ThreadGroup createProxy(ThreadGroup parent) {
        return (ThreadGroup) Proxy.newProxyInstance(
                ThreadGroup.class.getClassLoader(),
                new Class<?>[] { ThreadGroup.class },
                new ThreadGroupInvocationHandler(parent)
        );
    }

    private static class ThreadGroupInvocationHandler implements InvocationHandler {
        private final ThreadGroup realThreadGroup;

        public ThreadGroupInvocationHandler(ThreadGroup realThreadGroup) {
            this.realThreadGroup = realThreadGroup;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 在这里可以添加自定义逻辑
            System.out.println("Intercepted method: " + method.getName());
            return method.invoke(realThreadGroup, args);
        }
    }
}
