from kivymd.app import MDApp
from kivymd.uix.screen import MDScreen
from kivymd.uix.label import MDLabel
from kivy.core.window import Window
from kivy.utils import platform

class KartyApp(MDApp):
    def build(self):
        # Nastavení tématu aplikace
        self.theme_cls.primary_palette = "Blue"
        self.theme_cls.theme_style = "Light"

        # Vytvoření hlavní obrazovky
        screen = MDScreen()

        # Přidání nadpisu "KARTY"
        label = MDLabel(
            text="KARTY",
            halign="center",
            font_style="H1",
            pos_hint={"center_x": 0.5, "center_y": 0.5}
        )

        screen.add_widget(label)
        return screen

if __name__ == '__main__':
    # Spuštění aplikace
    KartyApp().run()
