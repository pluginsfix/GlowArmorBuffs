# GlowArmorBuffs

[![GitHub Release](https://img.shields.io/github/v/release/pluginsfix/GlowArmorBuffs?color=orange&logo=github)](https://github.com/pluginsfix/GlowArmorBuffs/releases)
[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Platform](https://img.shields.io/badge/Platform-Paper%20%7C%20Purpur%20%7C%20Folia-brightgreen.svg)](https://papermc.io/)

**GlowArmorBuffs** — плагин для серверов Minecraft (Paper, Purpur, Folia), позволяющий накладывать постоянные эффекты зелий на элементы брони (шлем, нагрудник, штаны, ботинки) с полноценной системой прокачки через интерактивное GUI-меню `/brewing`.

---

## ⚡ Возможности

- **Эффекты на броне**: наложение эффектов (Сила, Скорость, Регенерация, Огнестойкость, Спешка и др.) на отдельные элементы экипировки.
- **Динамическое применение**: бафы активны только пока броня надета, и моментально снимаются при снятии.
- **Система прокачки**: покупка и улучшение уровней эффектов за валюту PlayerPoints.
- **Интерактивное меню `/brewing`**: удобный GUI с предпросмотром брони в руке, баланса игрока и статуса улучшения.
- **Лимиты эффектов**: настраиваемый лимит количества бафов на одном предмете.
- **PDC & Lore**: сохранение данных через PersistentDataContainer и автообновление описания предмета с поддержкой HEX-цветов.
- **Folia & Paper Ready**: оптимизированные слушатели и планировщик без утечек памяти.

---

## 🎮 Команды и права

| Команда | Описание | Право |
|---|---|---|
| `/glowarmorbuffs <эффект> [уровень]` | Наложить эффект на броню в руке | `glowarmorbuffs.apply` |
| `/glowarmorbuffs remove` | Снять все эффекты с брони в руке | `glowarmorbuffs.remove` |
| `/glowarmorbuffs menu` | Открыть меню прокачки эффектов | `glowarmorbuffs.menu` |
| `/brewing` | Открыть меню прокачки эффектов | `glowarmorbuffs.menu` |
| `/glowarmorbuffs give <игрок> <эффект> [уровень]` | Выдать эффект игроку | `glowarmorbuffs.admin` |
| `/glowarmorbuffs reload` | Перезагрузить конфигурацию плагина | `glowarmorbuffs.admin` |

---

## ⚙️ Требования и интеграции

- **Java**: 21+
- **Сервер**: Paper / Purpur / Folia 1.20+
- **PlayerPoints** (опционально): покупка и улучшение эффектов за рубины/очки.
- **PlaceholderAPI** (опционально): плейсхолдеры в меню и сообщениях.

---

## 🛠️ Сборка

```bash
mvn clean package
```

Скомпилированный `.jar` файл будет создан в папке `target/`.
