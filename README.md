java-operator-sdk\
**Description** \
 A java operator that download helm charts from a repository, convert the chart into 
 kubernetes manifests and apply on cluster.Also do the reconcile when any of the resources are deleted, recreating them\
**How to use** \
**Requirements**

    Java 11
    Maven
    Helm
    Local K8s cluster(kind,minikube)

**Running on terminal**

Inside the root folder, run the following command:
    
    kubectl apply -f charles-deployment-crd.yaml
    chmod +x ./run.sh
    ./run.sh

After the application starts, run: \
     ```kubectl apply -f charles-deployment.yaml```
