package com.hbo.serviceregistry.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
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
    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistryController.class);

    private static final ResourceNotFoundException NOT_FOUND_EXCEPTION = new ResourceNotFoundException();


    private Map<String, Service> registry = new HashMap<>();

    private final String registrationUrl;
    private final ExecutorService executor = Executors.newFixedThreadPool(1000, new ThreadFactory() {
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
    public ServiceRegistryController(@Value("${registration}") final String registrationUrl) {
        this.registrationUrl = registrationUrl;

        loadRegistration();
    }

    /**
     * Loads our registrations from our external resource.
     * @return ServiceReload The details of the reload
     */
    private ServiceReload loadRegistration() {
        try {
            Map<String, Service> registrations = new HashMap<>();

            final RestTemplate restTemplate = new RestTemplate();
            final String response = restTemplate.getForObject(registrationUrl, String.class);

            for (String r : response.split("\n")) {
                if (r.length() > 0) {
                    String[] serviceRegistration = r.split("=");
                    if (serviceRegistration.length == 2) {
                        final String serviceName = serviceRegistration[0].trim();
                        final String serviceUrl = serviceRegistration[1].trim().replace("\n", "");
                        if (serviceName.length() > 0 && serviceUrl.length() > 0) {
                            registrations.put(serviceName, new Service(serviceName, serviceUrl));
                        }
                    }
                }
            }

            final int currentRegistrations = registry.size();
            if (registrations.size() > 0) {
                registry = registrations;
            }

            return new ServiceReload(registrationUrl, currentRegistrations, registry.size());

        } catch (Throwable t) {
            logger.error("Failed loading service registrations", t);
            throw new RuntimeException("Failed loading our registrations");
        }
    }

    /**
     * Looks up a service from our registry - synchronous request.
     *
     * @param servicename The logical name of the service
     * @return Service The service
     */
    @RequestMapping(path = "/slookup/{servicename}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public Service lookup(@PathVariable("servicename") final String servicename) {
        return Optional.ofNullable(registry.get(servicename))
                .orElseThrow(() -> NOT_FOUND_EXCEPTION);
    }

    /**
     * Looks up a service from our registry - non-blocking request.
     *
     * @param servicename The logical name of the service
     * @return DeferredResult&lt;Service&gt; The service
     */
    @RequestMapping(path = "/lookup/{servicename}", method = RequestMethod.GET, produces = "application/json")
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

    /**
     * Adds a temporary registry of new service.
     *
     * @param service The service details
     * @return DeferredResult&lt;Service&gt; The service details
     */
    @RequestMapping(path ="/register", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public DeferredResult<Service> register(@RequestBody final Service service) {
        final DeferredResult<Service> deferredResult = new DeferredResult<>();

        executor.submit(() -> {
            registry.put(service.getName(), service);
            deferredResult.setResult(service);
        });

        return deferredResult;
    }

    /**
     * Reloads our cache of service registrations.
     *
     * @return DeferredResult&lt;ServiceReload&gt; The reload details
     */
    @RequestMapping(path ="/reload", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public DeferredResult<ServiceReload> reload() {
        final DeferredResult<ServiceReload> deferredResult = new DeferredResult<>();

        executor.submit(() -> {
            try {
                deferredResult.setResult(loadRegistration());
            } catch (Throwable t) {
                deferredResult.setErrorResult(t);
            }
        });

        return deferredResult;
    }

}
