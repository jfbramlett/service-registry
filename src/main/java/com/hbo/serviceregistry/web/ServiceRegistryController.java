package com.hbo.serviceregistry.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Simple controller for our service registry.
 */
@Controller
public class ServiceRegistryController {
    private static final ResourceNotFoundException NOT_FOUND_EXCEPTION = new ResourceNotFoundException();

    private Map<String, Service> registry = new HashMap<>();
    private ExecutorService executor = Executors.newFixedThreadPool(1000, new ThreadFactory() {
        private int threadCount = 1;
        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("worker-" + threadCount);
            threadCount++;
            return t;
        }
    });


    /**
     * Constructor.
     */
    @Autowired
    public ServiceRegistryController() {
        for (int i = 0; i < 200; i++) {
            registry.put("service" + i, new Service("https://service" + i + ".hbo.com"));
        }
    }

    @RequestMapping(path = "/lookup/{servicename}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Service lookup(@PathVariable("servicename") final String servicename) {
        return Optional.ofNullable(registry.get(servicename))
                .orElseThrow(() -> NOT_FOUND_EXCEPTION);
    }

    @RequestMapping(path = "/dlookup/{servicename}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public DeferredResult<Service> dlookup(@PathVariable("servicename") final String servicename) {
        final DeferredResult<Service> deferredResult = new DeferredResult<>();

        executor.submit(() -> {
            try {
                deferredResult.setResult(lookup(servicename));
            } catch (RuntimeException rte) {
                deferredResult.setErrorResult(rte);
            }
        });

        return deferredResult;
    }
}
