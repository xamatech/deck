# Deck

Deck is a Service Registry + Load Balancer based on
[Spring Cloud Eureka](https://cloud.spring.io/spring-cloud-static/Greenwich.SR1/single/spring-cloud.html#_service_discovery_eureka_clients).

All services which regsitered to Deck can be accessed via Service Discovery
clients or are load balanced under a service id specific path. For example,#
if one or more [Dead Blind](https://github.com/xamatech/dead-blind) instances
register to `Deck`, the services can be accessed through a Load Balancer
with `localhost:8761/services/dead-blind-develop`:

- port 8671 is the default Deck port
- `services` is the standard prefix for all dynamically registered services
- `dead-blind-develop` is the service id und which the instances registered to

**Exemplary Request:** `GET localhost:8761/services/dead-blind-develop/heartbeat`


See also:
- [Eureka Dashboard](http://localhost:8761/) to access Eureka status of Deck