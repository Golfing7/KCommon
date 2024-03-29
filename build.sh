if ! [ -d BuildTools ]
then
  mkdir -p BuildTools
  cd BuildTools || exit
  curl -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
  if ! [ -f "spigot-1.19.2.jar" ]
   then
    java -jar BuildTools.jar --rev 1.19.2 --remapped
  fi
fi
