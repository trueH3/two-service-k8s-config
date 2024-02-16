Kubernetes ze str https://www.youtube.com/watch?v=X48VuDVv0do     

LEKCJA 1 - podstawowe elementy

Worker node - > node to serwer, albo virtualka albo fizyczny  w nodzie jest sieć wewnętrzna i każdy pod ma swój wewnętrzny adres ip. ale za każdym razem jak pod pada i jest stawiany na nowo to ma nowy adres ip, więc nie ma sensu się na niego zapinać. Aby wyeliminować tą niedogodność wprowadzono komponent o nazwie service . service to nic innego jak statyczny adres ip który może być przyporządkowany każdemu podowi  ConfigMap - tam mogę trzymać propertki do projektów ( np url do bazy) i nie trzymać ich bezpośrednio w serwisie. załączam taką config mapę do poda i działa. pamiętać należy żę to plaintextem jest przetrzymywane więc. sekretów tam się nie trzyma  sekrety trzyma się w elemencie zwanym Secret  Volume element- należy pamiętać że jak pod z bazą padnie, to dane wewnątrz bazy przepadają, no chyba że skonfigurowaliśmy volume (dokładnie jak w przypadku dockera) volume może być zdefiniowany wewn node’a (local) albo nawet na zewnątrz kubernetesa w postaci fizycznego nośnika (remote)   service w przypadku skalowalności horyzontalnej może być przyporządkowany do np 2 podów z tą samą apką, wtedy również pełni funkcję loadbalancera  no i właśnie dochodzimy do replikacji, dane ile czego i w jaki sposób ma być zreplikowane przechowywane są w komponencie o nazwie Deployment. Deploymentu jednak używamy tylko do serwisów nie bazodanowych. w przypadku repolikacji serwisów bazodanowych musimy użyć innego komponentu. No bo gdybyśmy użyli Deployment to obie repliki korzystają z tego samego volume i zapis/odczyt do volume musi odbywać się z zachowaniem reguł które to wykluczą data inconsistency  Dlatego do tego rodzaju replikacji używamy komponentu o nazwie StatefulSet. StatefulSet jest trudny w konfiguracji dlatego zaleca się trzymanie bazy poza klastrem kubernetesa    LEKCJA 2  - architektura kubernetesa   2 rodzaje node’ów  master slave  w każdym worker nodzie są 3 procesy: -container runtime -> jeśli kontenery to docker wtedy docker runtime - to jest logiczne kubek musi wiedzieć jak się porozumieć z technologią jaka została użyta do konteneryzacji
-kubelet odpowiada za harmonogramowanie podów. porozumiewa się z kontenerem jak i nodem. proces ten przyporządkowuje zasoby node’a do
-kube proxy - przekazuje requesty, wspomaga ruch wewn node’a i pomiędzy nodeami  master node odpowiedzialny jest za:
monitorowanie całości restart podów  tworzenie node’ów  w każdym master nodzie są 4 procesy API server - no to taki gateway do klastra , może to być cli, może być jakaś nakładka graficzna - dzięki temu api możemy wysyłać zapytania o stan klastra i także na niego wpływać, oczywiście wszystko jest robione po uprzedniej autentykacji
Scheduler - jeśli wysyłamy request do stworzenia poda to on jest kierowany do schedulera ,a ten natomiast wie dokładnie gdzie go wstawić i robi to optymalnie . patrzy na zasoby node’a i tam gdzie jest ich najwięcej tam go wpycha, ale sam ich nie tworzy (node’ów w sensie). tworzy je kubelet na worker nodzie, to on dostaje requesta od schedulera i robi robotę
Controller manager - potrafi wykryć padnięte pody
etcd - key value storage stanu klastra, taki jego mózg powiedzmy, tam są storowane wsszystkie dane np ile zasóbów ma dany node wolnych jeszcze   LEKCJA 3 - minikube and kubectl local setup  minikube ma tylko jednego node’a w którym działają procesy zarówno mastera jak i workera - dlatego jest to tylko na użytek lokalny na produkcji master i worker powinny być na różnych node’ach, dodatkowo ma preinstalowany container runtime w postaci dokera  instalacja to najlepiej wjeść na stronę minikube, moeć na maku zoinstalowanego doker desktop jako hypervisora, brew install minikube i tyle   żeby stworzyć docker image na podstawie Dockerfile to  -> docker build -t trueh3/proxyservice .        

LEKCJA 4 - main kubectl commands  żeby wrzucić jakiegoś poda to muszę   kubectl create deployment NAME —image=image  przykład   kubectl create deployment ngnix-deployment —image=nginx  kubectl get deployment -> listuje deploymenty  żeby podejrzeć logi apki w podzie to  -> kubectl logs <nazwa_poda>  żeby wejść w terminal kontenera to kubectl exec -it <pod_name> (it - interactive terminal) sh  żeby pozbyć się podów no to muszę usunąć deploymenty -> kubectl delete deployment <deployment_name> 
żeby wykonać akcję zdefiniowaną w pliku (np deployment) no to korzystam z : -> kubectl apply  -f <path_to_file>

kubectl get pods -o wide -> zwraca więcej detali poda , w tym jego zmienne ip   LEKCJA 5 - K8s configuration file  Każdy config file kubernetesa składa się z 3 części -metadata
-specification -status ( to jest generowane automatycznie przez kubernetesa  przykład service.yaml   apiVersion: v1
kind: Service
metadata:
name: proxyservice-service
spec:
selector:
app: proxyservice
ports:
- protocol: TCP
port: 80
targetPort: 8080
 przykład deployment.yaml
 apiVersion: apps/v1
kind: Deployment
metadata:
name: proxyservice-deployment
labels:
app: proxyservice
spec:
replicas: 1
selector:
matchLabels:
app: proxyservice
template:
metadata:
labels:
app: proxyservice
spec:
containers:
- name: proxyservice
image: trueh3/proxyservice:latest
ports:
- containerPort: 8080

jak widać w deployment.yaml w spec -> template ma własne metadata i spec więc można powiedzieć że to connfig w configu. część template ma w sobie dane o podzie ( z jakiego obrazu, jaki port wystawić, jak nazwać kontener)  w metadata labels to nic innego jak dowolne wartości key-value  żeby zweryfikować poprawne mapowanie portów serwisu i deploymentu to muszę zrobić 
 kubectl get pods -o wide  to da mi ip poda, potem robię kubectl describe service <service_name> i patrzę że endpoints ma ip poda  natomiast jest on mapowany na to co pokazuje kubectl get service , tam mam clusterIp oraz port 80 który definiuję w service.yaml   LEKCJA 6 - przykładowa aplikacja   external service vs ingress - obie rzeczy wystawiają adresy na świat,  definiując external service nadaję external ip , do tego jeśli jest skalowane to dochodzi loadbalancer, teraz wyobraźmy sobie sytuację w której mam kilka takich serwisów, za każdym razem muszę wtedy mieć osobne ip i loadbalacer, duży nakład i zwiększone koszty, w celu uniknięcia tego problemu stosowany jest ingress .  po stworzeniu sekreta, w celu weryfikacji czy zmienna została prawidłowo ustawiona mogę wbić się na poda wykonując komendę   kubectl exec -it <pod_name> sh, i jak już jestem w środku to mogę wylistować zmienne komendą env  aby nadać ip zewnętrzne w kubernetesie to wystarczy poprawnie zdefiniować serwis ale w minikube dodatkowo po wszystkim muszę wykonać komendę  minikube service <service_name> ponieważ jeśli tego nie zrobię to jak listuję serwisy to ciągle mam external ip -> pending  LEKCJA 7 - namespaces  służy do porządkowania zasobów w klastrze. niektórzy mówią że to klaster w klastrze  kubectl get namespace -> listuje ns kubectl create namespace <moja_nazwa> -> tworzy nową NS  namespace można tak skonfigurować aby tylko część osób miała do niego dostęp, można również ustaawić resource quota per namespace, ograniczając w ten sposób dowolnie zasoby per namespace  secret, i configmap zdefiniowana w 1 ns nie jest dostępna w 2 ns ale serwisy są  niektóre komponenty nie  są wstawiane per namespace np volume, node    aby wylistować co jest w namespace a co poza to  kubectl api-resources --namespaced=true -> dla tych co są  kubectl api-resources --namespaced=false -> dla tych co nie są  podczas definiowania komponentów w metadata możemy zdefiniować gdzie dany komponent ma powstać. Jeśli tego nie zrobimy to wejdzie do default  no dobra to jak to zrobić? dodać w sekcji metadata namespace: my-namespace  wtedy żeby znależć dany komponent np configMapę to muszę  kubectl get configmap -n my-namespace  po pewnym czasie może to być irytujące ciągłe dodawanie -n my-namespace  możemy zmienić default ns w kubku (bo na początku default to default dlatego nie muszę za każdym razem pisać -n default)  można zainstalować kubectx -> brew install kubectx  po instalacji jak wpiszę kubens to listuje wszystkie ns z podświetloną która jest default. dalej, wpisując kubens <nazwa ns> -> zmienia ją na default  LEKCJA 8 ingress  to że dam plik konfiguracyjny ingressa to nie wszystko , potrzebuję jeszcze dla niego implementacji. ta implementacja nazywa się Ingress controller i to jest nasz pkt 1 . taki ingress controller to może być oddzielna aplikacja działająca na podzie. no i ten kontroler by ewaluował reguły ustawione w pliku konfiguracyjnym ingressa. (rules to sekcja zdefiniowana w ingress.yaml)  jedną z takich implementacji jest k8s Ngnix ingress controller  no i tak naprawdę ok jeśli mamy kubka osadzonego na jakimś cloudzie to zapewne ma on  jakiś cloud load balancer wbudowany i to jest wtedy mój entry point do apki, z tego LB idzie sobie ruch do ingress controllera , potem do serwisu i wpada do poda. zatem nie muszę implementować load balancera sam. no ale jeśli robię to bez chmury no to muszę sam go zaimplementować  no dobra. jeśli chodzi o minikube żeby mieć ingress controllera to wykonuję minikube addons enable ingress,   i to pod spodem odpala poda z k8s ngnx ingress controller żeby skontrolować czy działa to jak wylistuję wszystkie namespace to powinna się pojawić ingress-nginx 

LEKCJA 9 helm charts  helm chart to tak naprawdę paczka plików konfiguracyjnych ( deploymenty, serwisy, stateful… może i ingressy ) jak ktoś je zdefiniuje dla jakiegoś stacka ( np elastic searcha) to inny może je zaciągnąć i z nich korzystać  kolejnym zadaniem jaki spełnia helmchart to template engine (czyli to co się powtarza w plikach mogę za pomocą zmiennych wkejać i to jest to co mnie właśnie interesuje)

kolejnym zaddaniem jest release management - może wersjonować releasy

struktura chartów  główny folder  - Chart.yaml
- values.yaml
- charts/
- templates/  i po kolei  Chart.yaml - zawiera podstawowe dane na temach wersji, nazwy chartów użytych, takie metadata values - to zestaw wartości które są wstrzykiwane w templates ( mogą one być nadpisane)
  charts/ tam idą inne helm charty jeśli nasze są od nich zależne templates/ - tam idą nasze pliki koonfiguracyjne ja: services, deployments, configMap itd

żeby zainstalować charty to robimy helm install <chart_name>   LEKCJA 12 kubernetes services

ClusterIP service NodePort Service Headless Service
LoadBalancer service  ogólnie services są po to aby - stable ip address - loadbalancing  najpowszechniejszym typem jest ClusterIP service, to jest defaultowy typ, taki zostanie stworzony jak w configu nie podam jego typu