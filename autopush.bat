@echo off
:loop
cd /d "E:\colonydirect"
git pull origin main
git add .
git commit -m "Auto-saved backup change"
git push origin main
timeout /t 300
goto loop
