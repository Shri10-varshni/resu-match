# ResuMatch

ResuMatch is a Java 25 / Spring Boot 3 microservice that recommends student projects for a resume based on job-description keywords and project timeline recency.

## Run locally

Requirements: Java 25 and Maven.

```sh
mvn spring-boot:run
```

The in-memory H2 database is initialized from `src/main/resources/data.sql`. Health and readiness checks are available at `/actuator/health` and `/actuator/health/readiness`; Prometheus metrics are exposed at `/actuator/prometheus`.

## Optimize a resume

```sh
curl -X POST http://localhost:8081/api/v1/optimize-resume \
  -H 'Content-Type: application/json' \
  -d '{"jobDescription":"Java Spring Boot REST API developer"}'
```

The response contains projects with at least one matching keyword, ordered by score. The score is a 0–100 weighted sum: 80% keyword coverage and 20% recency. A project with no end date is treated as current.

## Test and build

```sh
mvn -B clean verify
docker build -t resumatch:local .
docker run --rm -p 8081:8081 resumatch:local
```

## CI/CD prerequisites

Configure Jenkins with a JDK tool named `Java25` (Java 25), Maven, Docker, Gitleaks, `kubectl`, `k3s`, and a SonarQube server named `SonarQube`. The Jenkins agent must run on the K3s host, have Docker access, and be allowed to run `sudo k3s ctr images import -` without a password. On the main branch, the pipeline builds and imports the image into K3s containerd, then applies the manifests to the current kubectl context.

## Deploy locally with K3s

Requirements: K3s, kubectl, and Docker. Install K3s with the Ansible playbook below, or use the official K3s installation instructions.

```sh
docker build -t resumatch:local .
docker save --output resumatch-image.tar resumatch:local
sudo k3s ctr images import resumatch-image.tar
rm resumatch-image.tar
kubectl apply -f k8s/
kubectl rollout status deployment/resumatch
kubectl port-forward service/resumatch 8081:80
```

The playbook installs a kubeconfig at `~/.kube/config` for the Ansible user. K3s and the Kubernetes manifests use the standard Kubernetes API. To deploy to a cloud cluster later, update the active kubeconfig/context, push the image to a registry reachable by the cluster, and set the Deployment image to that registry path (for example, `kubectl set image deployment/resumatch resumatch=registry.example.com/resumatch:1.0.0`).

```sh
curl -X POST http://localhost:8081/api/v1/optimize-resume \
  -H 'Content-Type: application/json' \
  -d '{"jobDescription":"Java Spring Boot REST API developer"}'
```

## Host setup with Ansible

Update `ansible/inventory.ini` and install the Docker collection before running:

```sh
ansible-galaxy collection install -r ansible/requirements.yml
ansible-playbook -i ansible/inventory.ini ansible/setup.yml
```

The playbook installs Docker Engine, K3s, Gitleaks (x86_64 or ARM64), and runs SonarQube Community on port 9000. It adds the Ansible SSH user to the Docker group; reconnect after running it for Docker group membership to take effect. Change the pinned Gitleaks/K3s versions and SonarQube image in `ansible/setup.yml` to match your maintenance policy.

## Monitoring

`monitoring/prometheus.yml` scrapes the service's Prometheus endpoint. Grafana datasource and dashboard provisioning are in `monitoring/grafana/provisioning/`; mount `monitoring/grafana/dashboards` at `/var/lib/grafana/dashboards` in Grafana. Deploy Prometheus and Grafana separately using your platform's standard deployment process.
