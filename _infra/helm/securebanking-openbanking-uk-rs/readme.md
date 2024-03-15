# Secure API Gateway - Test Bank Facility (RS)

See [README](https://github.com/SecureApiGateway/secure-api-gateway-ob-uk-rs/blob/master/README.md) for information on RS

## Prerequisites

- Kubernetes v1.23 +
- Helm 3.0.0 +

To add the forgerock helm artifactory repository to your local machine to consume helm charts use the following;

```console
  helm repo add forgerock-helm https://maven.forgerock.org/artifactory/forgerock-helm-virtual/ --username [backstage_username]  --password [backstage_password]
  helm repo update
```

NOTE: You must have a valid [subscription](https://backstage.forgerock.com/knowledge/kb/article/a57648047#XAYQfS) to aquire the `backstage_username` and `backstage_password` values.

## Helm Charts
### Deployment
RS should only be installed as part of the [secure-api-gateway umbarella chart](https://github.com/SecureApiGateway/secure-api-gateway-releases/tree/master/secure-api-gateway) and not standalone from this repositry.  

However, as part of the deployment of the secure-api-gateway, you must build the java artifacts and built the docker image via the [Makefile](https://github.com/SecureApiGateway/secure-api-gateway-ob-uk-rs/blob/master/Makefile). 

Only once this has been done for all the components, can the [steps to deploy](https://github.com/SecureApiGateway/secure-api-gateway-releases/tree/master/secure-api-gateway/readme.md) the secure-api-gateway be performed.

### Example Manifest
This is an example manifest using the `values.yaml` file provided, there is no overlay values in this generated manifest hence why there is no repo URL in `spec.template.spec.containers.0.image`.

```yaml
---
# Source: securebanking-openbanking-uk-rs/templates/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: application-config
  labels:
    app: test-facility-bank
data:
  application.yml: | 
    example
---
# Source: securebanking-openbanking-uk-rs/templates/service.yaml
apiVersion: v1
kind: Service
metadata:
  name: test-facility-bank
  labels:
    app: test-facility-bank
spec:
  type: ClusterIP
  ports:
  - name: test-facility-bank
    port: 8080
    targetPort: 8080
    protocol: TCP
  selector:
    app: test-facility-bank
---
# Source: securebanking-openbanking-uk-rs/templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: test-facility-bank
  labels:
    app: test-facility-bank
spec:
  replicas: 1
  strategy:
    type: RollingUpdate
    
    rollingUpdate:
      maxSurge: 50%
      maxUnavailable: 25%

  selector:
    matchLabels:
      app: test-facility-bank
  template:
    metadata:
      labels:
        app: test-facility-bank
        appVersion: 1.0.0
        helmVersion: 1.0.0
    spec:
      containers:
        - name: test-facility-bank
          image: ":1.0.0"
          imagePullPolicy: Always
          ports:
            - name: http-server
              containerPort: 8080
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            periodSeconds: 5
            failureThreshold: 3
            successThreshold: 1
            timeoutSeconds: 5
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8080
            initialDelaySeconds: 160
            periodSeconds: 5
            failureThreshold: 5
            successThreshold: 1
            timeoutSeconds: 5
          volumeMounts:
            - name: spring-config
              mountPath: /app/config
          env:
          - name: SPRING_DATA_MONGODB_HOST
            value: mongodb
          - name: SERVER_PORT
            value: "8080"
          - name: CONSENT_REPO_URI
            valueFrom:
              configMapKeyRef:
                name: core-deployment-config
                key: CONSENT_REPO_URI
          - name: RS_DISCOVERY_FINANCIAL_ID
            valueFrom:
              configMapKeyRef:
                name: core-deployment-config
                key: OB_ASPSP_ORG_ID
          - name: SPRING_PROFILES_ACTIVE
            value: "docker"
          - name: JAVA_OPTS
            value: -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=50 -agentlib:jdwp=transport=dt_socket,address=*:9090,server=y,suspend=n
          resources:
            limits:
              cpu: 1
              memory: 1024Mi
            requests:
              cpu: 0.5
              memory: 512Mi
      volumes:
          - name: spring-config
            configMap:
              name: application-config
```
### Environment Variables

These are the environment variables declared in the `deployment.yaml`;
| Key | Default | Description | Source |
|-----|---------|-------------|--------|
| SPRING_DATA_MONGODB_HOST | mongodb | Full name of the mongodb deployment | deployment.mongodb.host |
| SERVER_PORT | 8080 | What port does the container use |deployment.containerPort |
| CONSENT_REPO_URI | http://ig:80 | URI of IG | core-deployment-config |
| RS_DISCOVERY_FINANCIAL_ID | 0015800001041REAAY | OB Organisation ID | core-deployment-config |
| SPRING_PROFILES_ACTIVE | docker |What spring provile to use | Hardcoded |
| JAVA_OPTS | -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=50 -agentlib:jdwp=transport=dt_socket,address=*:9090,server=y,suspend=n | Additional Java config | deployment.java.opts |

### Values
These are the values that are consumed in the `deployment.yaml` and `service.yaml`;
| Key | Type | Description | Default |
|-----|------|-------------|---------|
| configmap.apiVersion | string | Version of the Kubernetes API to use | apps/v1 |
| configmap.springConfig | object | additional config for Spring | {} |
| deployment.apiVersion | string | Version of the Kubernetes API to use | apps/v1 |
| deployment.containerPort | integer | Container port exposed by a pod or deployment | 8080 |
| deployment.image.repo | string | Repo to pull images from - Value should exist in values.yaml overlay in deployment repo | {} |
| deployment.image.tag | string | Tag to deploy - Value should exist in values.yaml overlay in deployment repo | {} |
| deployment.image.imagePullPolicy | string | Policy for pulling images | Always |
| deployment.java.opts | string | Additional Java config | -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=50 -agentlib:jdwp=transport=dt_socket,address=*:9091,server=y,suspend=n |
| deployment.livenessProbe.initialDelaySeconds | integer | Time to wait until liveness probe beings | 120 |
| deployment.livenessProbe.periodSeconds | integer | How long to test the probe | 5 |
| deployment.livenessProbe.failureThreshold | integer | How many times the probe can fail before declaring the pod unhealthy | 5 |
| deployment.livenessProbe.successThreshold | integer | How many times the prob must succeed before declaring the pod healthy | 1 |
| deployment.livenessProbe.timeoutSeconds | integer | Amount of time the probe will try hit the endpoint before declaring  5 |unsuccessful |
| deployment.mongodb.host | string | Full name of the mongodb deployment | mongodb |
| deployment.readinessProbe.periodSeconds | integer | How long to test the probe | 5 |
| deployment.readinessProbe.failureThreshold | integer | How many times the probe can fail before declaring the pod unhealthy | 3 |
| deployment.readinessProbe.successThreshold | integer| How many times the prob must succeed before declaring the pod healthy | | 1 
| deployment.readinessProbe.timeoutSeconds | integer | Amount of time the probe will try hit the endpoint before declaring unsuccessful | 5 |
| deployment.resources.limits.cpu | integer | Max amount of CPU the pod can consume | 0.5 |
| deployment.resources.limits.memory | string | Max amount of memory the pod can consume | 512Mi |
| deployment.resources.requests.cpu | integer | Minimum requested CPU required to run the pod | 0.25 |
| deployment.resources.requests.memory | string | Minimum requested memory required to run the pod | 256Mi |
| deployment.rollingUpdate.maxSurge | string | The maximum number of pods that can be scheduled above the desired number of pods | 50% |
| deployment.rollingUpdate.maxUnavailable | string | The maximum number of pods that can be unavailable during the update | 25% |
| deployment.starategyType | string | Type of deployment | RollingUpdate |
| service.apiVersion | string | Version of the Kubernetes API to use | v1 |
| service.port | integer | Container port exposed by a pod or deployment | 8080 |
| service.protocol | string | Protocol the service will use | TCP |
| service.targetPort | integer | Host Machine port that traffic is diverted too | 80 | 
| service.type | string | Type of service to create | ClusterIP |

NOTE: There is no `deployment.image.repo` or `deployment.image.tag` specified in the `Values.yaml` - This needs to be done in a seperate 'deployments' repo using an additional `values.yaml` overlay. You may overwrite any of the other values in this additonal file if required.

Example of the RCS section of the additonal `values.yaml` file;
```yaml
test-facility-bank:
  deployment:  
    image:
      repo: [REPO_URL]
      # By default the AppVersion will be used so that users don't have to change this value, however you can override this by uncommenting the line and providing a valid verison.
      # tag: 1.0.1
```
## Support

For any issues or questions, please raise an issue within the [SecureApiGateway](https://github.com/SecureApiGateway/SecureApiGateway/issues) repository.