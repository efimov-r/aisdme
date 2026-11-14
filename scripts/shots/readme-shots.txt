# Скриншоты для README: запускать на свежем приложении; PNG — build/debug-ctl/readme-light.png и readme-dark.png, скопировать в docs/
waitfor state 120000 proj/
arrow fold
arrow folda
node double main.asm
waitfor state 3000 *main.asm (writable)
node right notes.txt
waitfor state 2000 menu:Open in Right
tap menu:Open in Right
waitfor state 3000 notes.txt (writable)
node main.asm
tap tab:main.asm
move 600 650
wait 300
shot readme-light
tap settings
waitfor state 2000 menu:Dark
tap menu:Dark
waitfor state 2000 theme: dark
tap tab:main.asm
move 600 650
wait 300
shot readme-dark
tap settings
waitfor state 2000 menu:Light
tap menu:Light
waitfor state 2000 theme: light
