;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Original: https://github.com/technomancy/leiningen/src/leiningen/new/templates.clj
;;
(ns czlab.wabbit.shared.templates

  (:require [clojure.java.io :as io]
            [clojure.string :as cs])

  (:import [java.io BufferedReader File]
           [java.util Calendar]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; can't set this to stencil.core/render-string here because
;; pulling in the stencil lib in this library will cause
;; classloading issues when used by lein-wabbit as a
;; leiningen template.
(def ^:dynamic *renderer-fn* nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defmacro ^:private trap! "" [s] `(throw (Exception. (str ~s))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- fixLineSeps
  ""
  [s]
  (->> (if (System/getenv "LEIN_NEW_UNIX_NEWLINES")
         "\n"
         (System/getProperty "line.separator"))
       (cs/replace s "\n")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- slurpToLF
  ""
  [^BufferedReader r]
  (let [sb (StringBuilder.)]
    (loop [s (.readLine r)]
      (if (nil? s)
        (str sb)
        (do
          (.append sb s)
          (.append sb "\n")
          (recur (.readLine r)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- slurpResource
  ""
  [res]
  ; for 2.0.0 compatibility, can break in 3.0.0
  (-> (if (string? res)
        (io/resource res) res)
      io/reader slurpToLF fixLineSeps))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defmacro sanitize
  "" [s] `(clojure.string/replace ~s "-" "_"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn nameToPath
  ""
  [s]
  (-> (sanitize s)
      (cs/replace "." java.io.File/separator)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn sanitizeNsp
  ""
  [s]
  (-> (cs/replace s "/" ".")
      (cs/replace "_" "-")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn groupName
  ""
  [s]
  (let [grpseq (butlast (cs/split (sanitizeNsp s) #"\."))]
    (if (seq grpseq)
      (->> grpseq (interpose ".") (apply str)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn year
  ""
  [] (.get (Calendar/getInstance) Calendar/YEAR))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn date
  ""
  []
  (let [df (java.text.SimpleDateFormat. "yyyy-MM-dd")]
    (.format df (java.util.Date.))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- resPath
  ""
  [path]
  (let [p (cs/join "/"
                   ["czlab/wabbit/shared/new" path])]
    [p (io/resource p)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn renderer
  ""
  [name & [render-fn]]
  (let [render (or render-fn *renderer-fn*)]
    (fn [template & [data]]
      (let [[p r] (resPath template)]
        (if (some? r)
          (if data
            (render (slurpResource r) data)
            (io/reader r))
          (trap! (format
                   "Resource '%s' not found" p)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn rawResourcer
  ""
  [name]
  (fn [file]
    (let [[p r] (resPath file)]
      (if (some? r)
        (io/input-stream r)
        (trap! (format "Resource '%s' not found" p))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn- templatePath
  ""
  ^File
  [name path data]
  (io/file name (*renderer-fn* path data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;
(defn toFiles
  ""
  [{:keys [dir force?] :as cli-options}
   {:keys [name] :as data} & paths]
  (let []
    (if (or (= "." dir)
            (.mkdir (io/file dir)) force?)
      (doseq [path paths]
        (if (string? path)
          (.mkdirs (templatePath dir path data))
          (let [[path content & options] path
                path (templatePath dir path data)
                options (apply hash-map options)]
            (.mkdirs (.getParentFile path))
            (io/copy content (io/file path))
            (when (:executable options)
              (.setExecutable path true)))))
      (trap! (str "Could not create directory "
                  dir
                  ". Maybe it already exists?"
                  "  See also :force or --force")))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;EOF

