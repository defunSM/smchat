(ns smchat.core
  (:gen-class)
  (:import org.pushingpixels.substance.api.SubstanceLookAndFeel)
  (:require [clojure.string :as str]))

;; Make a settings frame.
;; Fix the way chat left over is dealt with in the sql database.
;; remove changing name option to adding a nickname
;; move registration to the site.

(use 'clojure.repl)
(use 'seesaw.core)
(use 'seesaw.font)
(use 'seesaw.dev)

(defn -main [& args]
  (println "GUI up and running...")

  (declare f)
  (declare display-area)
  (declare input-command)
  (declare login-frame)
  (declare register-frame)
  (declare username-input)
  (declare password-input)
  (declare ruser-text-field)
  (declare confirm-textbox)
  (declare remail-text)
  (declare rage-text)
  (declare cphone-text)


  (def chatname (atom "Unknown"))
  (def user-password (atom ""))
  (def chatserver (atom "http://servesm.herokuapp.com/"))
  (def nickname (atom ""))
  (def adminstatus (atom ""))
  (defonce chatcolor (atom "yellow"))


  (defn set-interval [callback ms]
    (future (while true (do (Thread/sleep ms) (callback)))))

  (def job (set-interval #(do (text! display-area (clojure.string/replace (slurp (str @chatserver "chat")) #"%20" " "))) 1000))

;; Keypress for the chatbox which will clear the input-command and send the new message once enter is pressed.

  (defn keypress [e]
    (let [k (.getKeyChar e)]
      (println k (type k))
      (if (= k \newline)
        (do (slurp (str @chatserver "chat?" @chatname @nickname "=>" (clojure.string/replace (text input-command) #" " "%20")))
            (text! input-command "")))))

  (def input-command (text :multi-line? false :text "" :listen [:key-typed keypress]))
  (def display-area (text :multi-line? true :text "You have launched ChatBox.\n\n\n\n\n" :foreground @chatcolor :background "black"))

  (def chat-prompt (label :text "=>"))
  (def chat-prompt-main (atom "=>"))

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

  (defn check-valid [name]
    (if (= nil (re-find #"[-!$%^&*_+~=`{}\[\]:;<>?,. \/]" name))
      (reset! nickname (str "(" name ")"))
      (alert "Please enter a valid nickname without spaces and symbols.")))

  (defn check-valid-prompt [prompt]
    (if (= nil (re-find #"[!$%^&*_+~=`{}\[\]:;?,. \/]" prompt))
      (do (config! chat-prompt :text prompt)
          (reset! chat-prompt-main prompt))
      (alert "Certain symbols are not allowed for the prompt.")))

  (defn handler [event]
    (let [e (.getActionCommand event)]
      (if (= e "Close ChatBox")
        (do (-> f hide!)))
      (if (= e "Clear Chat")
        (do (slurp (str @chatserver "clearchat"))))
      (if (= e "Change Chat Nickname")
        (do (check-valid (input "Enter the nickname you want in the chatbox: "))))
      (if (= e "Change ChatServer")
        (do (reset! chatserver (input "Enter the new ChatServer: "))))
      (if (= e "Change Chat Color")
        (do (reset! chatcolor (input "Enter Color: "))
            (config! display-area :foreground @chatcolor)))
      (if (= e "Change Theme")
        (do (-> (frame :title "Themes" :id 3 :content (theme-selector) :on-close :hide :height 600 :width 300) pack! show!)))
      (if (= e "Change Prompt")
        (do (check-valid-prompt (input "Enter new prompt: "))))
      (if (= e "Default Settings")
        (do (reset! chatcolor "yellow")
            (config! display-area :foreground @chatcolor)
            (SubstanceLookAndFeel/setSkin "org.pushingpixels.substance.api.skin.GraphiteAquaSkin")
            (config! chat-prompt :text "=>")
            (reset! chat-prompt-main "=>")
            (reset! chatname "Unknown")))
      (if (= e "Login")
        (do (reset! chatname (text username-input))
            (reset! user-password (text password-input))
            (-> login-frame hide!)
            (if (= "TRUE" (slurp (str @chatserver "confirmlogin?" (text username-input) "%20" (text password-input))))
              (-> f pack! show!)
              (do (alert "Invalid Username or Password")
                  (-> login-frame show!)))))
      (if (= e "Register")
        (do (-> login-frame hide!)
            (-> register-frame show!)))
      (if (= e "Guest")
        (do (-> login-frame hide!)
            (-> f pack! show!)
            (reset! chatname (str "Guest" (str (rand-int 1000))))
            (alert (str "You have logged in as " @chatname))))
      (if (= e "Continue")
        (do (slurp (str @chatserver "new?" (text ruser-text-field) "%20" (text confirm-textbox) "%20" (text remail-text) "%20" (text rage-text) "%20" (text cphone-text)))
            (-> register-frame hide!)
            (-> f pack! show!)
            (reset! chatname @text ruser-text-field)))
      (if (= e "Log into Admin Account")
        (do (if (= "smchatadmin" (input "Enter ADMIN Password: "))
              (do (reset! chatname (input "Enter UserName: "))
                  (reset! nickname "[[ADMIN]]")
                  (alert (str "You have logged in as " @chatname @nickname)))
              (alert "Incorrect Password!"))))))

  ;; something wrong with the continue handler
  ;; Add stuff so that it takes all the information and records it in a file

  (def close-chatbox (menu-item :text "Close ChatBox"
                                :tip "Closes the ChatBox."
                                :listen [:action handler]))

  (def enter-chat-name (menu-item :text "Change Chat Nickname"
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

  (def gain-admin (menu-item :text "Log into Admin Account"
                             :tip "You should know what this does."
                             :listen [:action handler]))

  ;; This is the main SMCHAT frame that is launched in the beginnning.

  (def f (frame :title "SMChat"
                :id 100
                :menubar (menubar :items [(menu :text "File" :items [close-chatbox])
                                          (menu :text "Customize" :items [change-chat-color change-prompt theme-select return-default])
                                          (menu :text "Chat" :items [clear-chat change-chatserver enter-chat-name gain-admin])
                                          (menu :text "Help" :items [documentation])])

                :height 300
                :width 300
                :on-close :hide
                :content (content)))

  ;; For the login FRAME ----------------------------------------------------------
  (def user-label (label :text "Username: "))
  (def username-input (text :text "" :foreground "yellow"))
  (def pass-label (label :text "Password: "))
  (def password-input (password))
  (def enter-login (button :text "Login" :listen [:action handler]))
  (def register-login (button :text "Register" :listen [:action handler]))
  (def guest-login (button :text "Guest" :listen [:action handler]))
  ;; ------------------------------------------------------------------------------

  (def ruser-label (label :text "Username: "))
  (def ruser-text-field (text :text "" :foreground "yellow"))
  (def ruser (horizontal-panel :items [ruser-label ruser-text-field]))
  (def rpass-label (label :text "Password: "))
  (def confirm-rpass (label :text "Confirm Password: "))
  (def confirm-textbox (password))
  (def rpass-text-field (password))
  (def rpass (horizontal-panel :items [rpass-label rpass-text-field]))
  (def rpass-confirm (horizontal-panel :items [confirm-rpass confirm-textbox]))
  (def remail-label (label :text "Email: "))
  (def remail-text (text :text "" :foreground "yellow"))
  (def remail (horizontal-panel :items [remail-label remail-text]))
  (def confirm-email-label (label :text "Confirm Email: "))
  (def confirm-email-text (text :text "" :foreground "yellow"))
  (def confirm-email (horizontal-panel :items [confirm-email-label confirm-email-text]))
  (def rage-label (label :text "Age: "))
  (def rage-text (text :text "" :foreground "yellow"))
  (def rage (horizontal-panel :items [rage-label rage-text]))
  (def register-done (button :text "Continue" :listen [:action handler]))
  (def rphone-label (label :text "Phone: "))
  (def rphone-text (text :text "" :foreground "yellow"))
  (def phone (horizontal-panel :items [rphone-label rphone-text]))
  (def cphone-label (label :text "Confirm Phone: "))
  (def cphone-text (text :text "" :foreground "yellow"))
  (def confirm-phone (horizontal-panel :items [cphone-label cphone-text]))

  (defn register-content []
    (vertical-panel :items [ruser rpass rpass-confirm remail confirm-email rage phone confirm-phone register-done]))

  (defn login-content []
    (vertical-panel :items [user-label username-input pass-label password-input (horizontal-panel :items [enter-login register-login guest-login])]))

  (def register-frame (frame :title "Register"
                             :id 414
                             :height 300
                             :width 300
                             :on-close :hide
                             :content (register-content)))

  (def login-frame (frame :title "Login"
                          :id 120
                          :height 300
                          :width 300
                          :on-close :hide
                          :content (login-content)))

  (native!)
  (invoke-later
   (-> login-frame pack! show!)
   (SubstanceLookAndFeel/setSkin "org.pushingpixels.substance.api.skin.GraphiteAquaSkin")
   (request-focus! input-command)))
