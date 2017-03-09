(ns handler-demo.application
  (:gen-class)
  (:require [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [environ.core :refer [env]]
            [com.stuartsierra.component :as component]
            [system.components.endpoint :refer [new-endpoint]]
            [system.components.handler :refer [new-handler]]
            [system.components.middleware :refer [new-middleware]]
            [system.components.jetty :refer [new-web-server]]
            [compojure.core :refer [GET POST]]))

(defn webhook-routes [_]
  (POST "/webhook" _
    {:status 200
     :body "webhook OK"}))

(defn app-routes [_]
  (GET "/" _
    {:status 200
     :body "app OK"}))

(defn csrf-middleware [handler]
  (fn [{:keys [request-method headers] :as req}]
    (if (and (= request-method :post) (not (some #{:x-csrf-token} headers)))
      {:status 403
       :body "POST requests must include CSRF token header"}
      (handler req))))

(defn prod-system [{:keys [web-port]}]
  (component/system-map
   :app-routes     (-> (new-endpoint app-routes)
                       (component/using [:middleware]))
   :webhook-routes (new-endpoint webhook-routes)
   :middleware     (new-middleware {:middleware [csrf-middleware]})
   :handler        (-> (new-handler)
                       (component/using [:webhook-routes :app-routes]))
   :jetty          (-> (new-web-server web-port)
                       (component/using [:handler]))))

(defn -main [& _]
  (component/start (prod-system {:web-port 10101})))
