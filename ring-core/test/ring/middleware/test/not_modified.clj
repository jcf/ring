(ns ring.middleware.test.not-modified
  (:use clojure.test
        ring.middleware.not-modified
        [ring.util.response :only [get-header]]))

(def weak-digest
  "W/3747421e99ba5399f6aecebb8c7fee3a")

(def date-older "Sat, 01 Jan 2000 12:00:00 GMT")
(def date-newer "Sun, 02 Jan 2000 12:00:00 GMT")

(def test-data
  #{{:desc "GET request with no novel headers"
     :request {:request-method :get :headers {}}
     :in      {:status 200 :headers {}}
     :out     {:status 200 :etag nil :last-modified nil}}

    {:desc "GET request with matching etag and if-none-match"
     :request {:request-method :get :headers {"if-none-match" weak-digest}}
     :in      {:status 200 :headers {"etag" weak-digest}}
     :out     {:status 304 :etag weak-digest :last-modified nil}}

    {:desc "GET request with last-modified after if-modified-since"
     :request {:request-method :get :headers {"if-modified-since" date-older}}
     :in      {:status 200 :headers {"last-modified" date-newer}}
     :out     {:status 200 :etag nil :last-modified date-newer}}

    {:desc "GET request with last-modified before if-modified-since"
     :request {:request-method :get :headers {"if-modified-since" date-newer}}
     :in      {:status 200 :headers {"last-modified" date-older}}
     :out     {:status 304 :etag nil :last-modified date-older}}

    {:desc "HEAD request with no novel headers"
     :request {:request-method :head :headers {}}
     :in      {:status 200 :headers {}}
     :out     {:status 200 :etag nil :last-modified nil}}

    {:desc "HEAD request with matching etag and if-none-match"
     :request {:request-method :head :headers {"if-none-match" weak-digest}}
     :in      {:status 200 :headers {"etag" weak-digest}}
     :out     {:status 304 :etag weak-digest :last-modified nil}}

    {:desc "HEAD request with last-modified after if-modified-since"
     :request {:request-method :head :headers {"if-modified-since" date-older}}
     :in      {:status 200 :headers {"last-modified" date-newer}}
     :out     {:status 200 :etag nil :last-modified date-newer}}

    {:desc "HEAD request with last-modified before if-modified-since"
     :request {:request-method :head :headers {"if-modified-since" date-newer}}
     :in      {:status 200 :headers {"last-modified" date-older}}
     :out     {:status 304 :etag nil :last-modified date-older}}

    {:desc "PUT request with etag"
     :request {:request-method :put}
     :in      {:status 200 :headers {"etag" weak-digest}}
     :out     {:status 200 :etag weak-digest :last-modified nil}}})

(deftest test-not-modified-response
  (doseq [{:keys [desc request in out]} test-data]
    (let [{:keys [status etag last-modified]} out
          response (not-modified-response in request)]
      (testing desc
        (is (= status (:status response)))
        (is (= etag (get-header response "etag")))
        (is (= last-modified (get-header response "last-modified")))))))
