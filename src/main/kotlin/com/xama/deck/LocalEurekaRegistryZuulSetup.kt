package com.xama.deck

import com.netflix.appinfo.InstanceInfo
import com.netflix.client.config.IClientConfig
import com.netflix.eureka.registry.InstanceRegistry
import com.netflix.loadbalancer.AbstractServerList
import com.netflix.loadbalancer.Server
import com.netflix.loadbalancer.ServerList
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer
import org.springframework.cloud.client.DefaultServiceInstance
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.client.discovery.event.HeartbeatEvent
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent
import org.springframework.cloud.netflix.ribbon.RibbonClients
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.net.URI
import java.util.*

@RibbonClients(defaultConfiguration = [RibbonConfig::class])
class RibbonDefaultConfig

@Configuration
class RibbonConfig {
    /**
     * Custom Server Listing for Ribbon Client utilised by Zuul. The server listing access instances registered by
     * local Eureka Server.
     */
    @Bean
    fun ribbonServerList(instanceRegistry: InstanceRegistry): ServerList<Server> {
        return EurekaRegistryBasedServerList(instanceRegistry) as ServerList<Server>
    }
}


@Configuration
class Config {
    /**
     * Event Listener to refresh Zuul Route mapping based Eureka Registry Events
     */
    @Bean
    fun eurekaToZuulTranslationEventListener(applicationEventPublisher: ApplicationEventPublisher): ApplicationListener<ApplicationEvent> {
        return ApplicationListener<ApplicationEvent> {
            when(it){
                is EurekaInstanceRegisteredEvent, is EurekaInstanceCanceledEvent -> {
                    applicationEventPublisher.publishEvent(HeartbeatEvent(
                            DeckApplication::class,
                            UUID.randomUUID() // make sure routing is ALWAYS refreshed
                    ))
                }
                is EurekaInstanceRenewedEvent -> {
                    applicationEventPublisher.publishEvent(HeartbeatEvent(
                           DeckApplication::class,
                           it.serverId // make sure routing is refreshed,IF NECESSARY
                    ))
                }
            }
        }
    }
}


/**
 * Custom Server Listing for Ribbon Client utilised by Zuul. The server listing access instances registered by
 * local Eureka Server.
 */
class EurekaRegistryBasedServerList(private val instanceRegistry: InstanceRegistry): AbstractServerList<DiscoveryEnabledServer>() {
    private var clientConfig: IClientConfig? = null

    override fun getInitialListOfServers(): List<DiscoveryEnabledServer> {
        return updatedListOfServers
    }

    override fun initWithNiwsConfig(clientConfig: IClientConfig?) {
        this.clientConfig = clientConfig
    }

    override fun getUpdatedListOfServers(): List<DiscoveryEnabledServer> {

        return instanceRegistry.applications
                ?.registeredApplications
                ?.flatMap { it.instances }
                ?.filter { it.status == InstanceInfo.InstanceStatus.UP }
                ?.map {  DiscoveryEnabledServer(it, false, false) }
                ?.toList() ?: listOf()

    }
}


/**
 * Discovery Client implementation accessing all instances registered by this Eureka Registry
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
class LocalDiscoveryClient(private val instanceRegistry: InstanceRegistry): DiscoveryClient {

    override fun getServices(): List<String> {
        val applications = this.instanceRegistry.applications ?: return emptyList()

        return applications.registeredApplications
                .asSequence()
                .filter { it.instances.isNotEmpty() }
                .map { it.name.toLowerCase() }
                .toList()
    }


    override fun getInstances(serviceId: String): List<ServiceInstance> {
        val applications = this.instanceRegistry.applications ?: return emptyList()
        val instances = applications.getInstancesBySecureVirtualHostName(serviceId) +
                        applications.getInstancesByVirtualHostName(serviceId)

        return instances.map { EurekaServiceInstance(it) }
    }


    fun getInstancesByVipAddress(vipAddress: String): List<InstanceInfo> {
        val applications = this.instanceRegistry.applications ?: return emptyList()
        return applications.getInstancesBySecureVirtualHostName(vipAddress) +
               applications.getInstancesByVirtualHostName(vipAddress)
    }


    override fun description(): String {
        return "Local Eureka Server - Server List"
    }

}


private class EurekaServiceInstance(val instanceInfo: InstanceInfo) : ServiceInstance {
    override fun getInstanceId(): String {
        return this.instanceInfo.id
    }

    override fun getServiceId(): String {
        return this.instanceInfo.appName
    }

    override fun getHost(): String {
        return this.instanceInfo.hostName
    }

    override fun getPort(): Int {
        return if (isSecure) {
            this.instanceInfo.securePort
        } else this.instanceInfo.port
    }

    override fun isSecure(): Boolean {
        // assume if secure is enabled, that is the default
        return this.instanceInfo.isPortEnabled(InstanceInfo.PortType.SECURE)
    }

    override fun getUri(): URI {
        return DefaultServiceInstance.getUri(this)
    }

    override fun getMetadata(): Map<String, String> {
        return this.instanceInfo.metadata
    }
}
