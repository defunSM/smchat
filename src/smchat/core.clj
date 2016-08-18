(ns smchat.core
  (:gen-class)
  (:import org.pushingpixels.substance.api.SubstanceLookAndFeel)
  (:require [clojure.string :as str]))

;; change code to work on a windows platform! (java.net.URL) to make it useo nly java without interactions with the terminal.
;; add documentation
;; TEST to work on windows and mae a windows .exe
;; Make the site launching smchat on defunsm.github
;; Add download links for both the .exe and the linux on that has to be made.
;; make a github repository for this.

;; REMOVE a lot of useless and bad code from this.
;; Think of more ideas and start working on the postgrel database.
;; Make a settings frame.


(use 'clojure.repl)
(use 'seesaw.core)
(use 'seesaw.font)
(use 'seesaw.dev)
(use 'clojure.java.shell) ;; This use will be removed once the terminal interactions are gone and works on windows.

(defn -main [& args]
  (println "GUI up and running...")

  (declare f)
  (declare display-area)
  (declare input-command)
  (def chatname (atom "Unknown"))
  (def chatserver (atom "http://servesm.herokuapp.com/"))
  (defonce chatcolor (atom "yellow"))

  ;; this won't even be needed once replacing the terminal interaction. so this will be removed soon!

  (defn sh-command [command-args]
  (let [val (str/split command-args #" ")
        counter (count val)]
    (if (= counter 1)
      (text! display-area (:out (sh (first val)))))
    (if (= counter 2)
      (text! display-area (:out (sh (first val) (second val)))))
    (if (= counter 3)
      (text! display-area (:out (sh (first val) (second val) (nth val 2)))))
    (if (= counter 4)
      (text! display-area (:out (sh (first val) (second val) (nth val 2) (nth val 3)))))
    (if (= counter 5)
      (text! display-area (:out (sh (first val) (second val) (nth val 2) (nth val 3) (nth val 4)))))
    (if (= counter 6)
      (text! display-area (:out (sh (first val) (second val) (nth val 2) (nth val 3) (nth val 4) (nth val 5)))))))

  ;; This stuff is fine for now but might need to be updated since it does require the terminal interaction to do the interval.

  (defn set-interval [callback ms]
    (future (while true (do (Thread/sleep ms) (callback)))))

  (def job (set-interval #(do (text! display-area (clojure.string/replace (slurp (str @chatserver "chat")) #"%20" " "))) 1000))

;; Keypress for the chatbox which will clear the input-command and send the new message once enter is pressed.

  (defn keypress [e]
    (let [k (.getKeyChar e)]
      (println k (type k))
      (if (= k \newline)
        (do (slurp (str @chatserver "chat?" @chatname "=>" (clojure.string/replace (text input-command) #" " "%20")))
            (text! input-command "")))))

  (def input-command (text :multi-line? false :text "" :listen [:key-typed keypress]))
  (def display-area (text :multi-line? true :text "You have launched ChatBox.\n\n\n\n\n" :foreground @chatcolor :background "black"))

  (def chat-prompt (label :text "=>"))

  (defn southcontent []
    (horizontal-panel :items [chat-prompt input-command]))

  ;; This doesnt work yet but should be implemented as soon as postgrel is up on server

  (defn friends-list []
    (text :multi-line? true :text "Friends:\n\n" :foreground @chatcolor :background "black"))

  ;; not in use right now until it can be made to look better.

  (defn tabbed-version []
    (tabbed-panel :tabs [{:title "Chat" :content (scrollable display-area)}
                                  {:title "Friends" :content (friends-list)}]))

  (defn content []
    (border-panel
     :south (southcontent)
     :center (scrollable display-area)
     :vgap 5 :hgap 5 :border 5))

  ;; wrote strip-extra at 5 am so it is written poorly. Also used poorly could be better.

  (defn strip-extra [string]
    (second (re-find #"\.(.*)" string)))

  ;; This is the theme selector that pops up when selecting "Change theme" in the main frame's menu.

  (defn theme-selector []
    (horizontal-panel
     :items [
             (combobox
              :model    (vals (SubstanceLookAndFeel/getAllSkins))
              :renderer (fn [this {:keys [value]}]
                          (text! this (strip-extra (strip-extra (strip-extra (strip-extra (strip-extra (.getClassName value))))))))
              :listen   [:selection (fn [e]
                                      (invoke-later
                                       (-> e
                                           selection
                                           .getClassName
                                           SubstanceLookAndFeel/setSkin)))])]))

  ;; This is the handler for the menu of the main frame that is being launched.

  (defn handler [event]
    (let [e (.getActionCommand event)]
      (if (= e "Close ChatBox")
        (do (-> f hide!)))
      (if (= e "Refresh ChatBox")
        (do (slurp (str "curl" @chatserver "chat?"))))
      (if (= e "Clear Chat")
        (do (slurp (str @chatserver "clearchat"))))
      (if (= e "Change Chat Name")
        (do (reset! chatname (input "Enter the name you want in the chatbox: "))))
      (if (= e "Change ChatServer")
        (do (reset! chatserver (input "Enter the new ChatServer: "))))
      (if (= e "Change Chat Color")
        (do (reset! chatcolor (input "Enter Color: "))
            (config! display-area :foreground @chatcolor)))
      (if (= e "Change Theme")
        (do (-> (frame :title "Themes" :id 3 :content (theme-selector) :on-close :hide :height 600 :width 300) pack! show!)))
      (if (= e "Change Prompt")
        (do (config! chat-prompt :text (input "Enter new prompt: "))))
      (if (= e "Default Settings")
        (do (reset! chatcolor "yellow")
            (config! display-area :foreground @chatcolor)
            (SubstanceLookAndFeel/setSkin "org.pushingpixels.substance.api.skin.GraphiteAquaSkin")
            (config! chat-prompt :text "=>")
            (reset! chatname "Unknown")))))

  (def close-chatbox (menu-item :text "Close ChatBox"
                                :tip "Closes the ChatBox."
                                :listen [:action handler]))

  (def refresh-chat (menu-item :text "Refresh ChatBox"
                               :tip "Displays the new messages in the display area."
                               :listen [:action handler]))

  (def enter-chat-name (menu-item :text "Change Chat Name"
                                  :tip "This allows you to change your chat name."
                                  :listen [:action handler]))

  (def clear-chat (menu-item :text "Clear Chat"
                             :tip "Clears the entire chat."
                             :listen [:action handler]))

  (def change-chatserver (menu-item :text "Change ChatServer"
                                    :tip "Changes the chatserver you are recieving messages from."
                                    :listen [:action handler]))

  (def change-chat-color (menu-item :text "Change Chat Color"
                                    :tip "Allows you to change the color of the chat."
                                    :listen [:action handler]))

  (def theme-select (menu-item :text "Change Theme"
                               :tip "Allows you to change the theme."
                               :listen [:action handler]))

  ;; Has not been added to the handler yet.

  (def documentation (menu-item :text "Documentation"
                                :tip "Opens up a browser to show documentaiton."
                                :listen [:action handler]))

  (def change-prompt (menu-item :text "Change Prompt"
                                :tip "This changes the prompt which is by default =>"
                                :listen [:action handler]))

  ;; Changes font-color, prompt, chatname, and theme.

  (def return-default (menu-item :text "Default Settings"
                                 :tip "Clicking this returns all default settings."
                                 :listen [:action handler]))

  ;; This is the main SMCHAT frame that is launched in the beginnning.

  (def f (frame :title "SMChat"
                :id 100
                :menubar (menubar :items [(menu :text "File" :items [close-chatbox])
                                          (menu :text "Customize" :items [change-chat-color change-prompt theme-select return-default])
                                          (menu :text "Chat" :items [refresh-chat clear-chat change-chatserver enter-chat-name])
                                          (menu :text "Help" :items [documentation])])

                :height 300
                :width 300
                :on-close :hide
                :content (content)))

  (native!)
  (invoke-later
   (-> f pack! show!)
   (SubstanceLookAndFeel/setSkin "org.pushingpixels.substance.api.skin.GraphiteAquaSkin")
   (request-focus! input-command)))
