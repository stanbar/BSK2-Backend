#!/bin/sh

USERNAME=""
PASSWORD=""
METHOD=""
COOKIEID=""
POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"
case $key in
    -u|--username|-l|--login)
    USERNAME="$2"
    shift # past argument
    shift # past value
    ;;
    -p|--password)
    PASSWORD="$2"
    shift # past argument
    shift # past value
    ;;
    -b|--cookie)
	COOKIEID="$2"
	shift
	shift
    ;;
    *)    # unknown option
	if [ -z "${METHOD}" ]; then
		METHOD="$1"
	elif [ -z "${USERNAME}" ]; then
		USERNAME="$1"
	elif [ -z "${PASSWORD}" ]; then
		PASSWORD="$1"
	else
		POSITIONAL+=("$1") # save it in an array for later
	fi
    shift # past argument
    ;;
esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters

if [ -z "${METHOD}" ]; then
	METHOD="login"
fi
if [ -z "${USERNAME}" ]; then
	USERNAME="admin"
fi
if [ -z "${PASSWORD}" ]; then
	PASSWORD="admin"
fi

case $METHOD in
		"signup")
	curl -X POST -v \
	http://localhost:8080/$METHOD \
	-H "Content-Type: application/x-www-form-urlencoded" \
	-d "username=${USERNAME}&password=${PASSWORD}"
	;;


	"login")
	curl -X GET -v \
	--basic --subject $USERNAME:$PASSWORD \
	http://localhost:8080/$METHOD \
	-H "Content-Type: application/x-www-form-urlencoded"
	;;

	*)
	curl -X GET -v \
	-b "SESSIONID=${COOKIEID}" \
	http://localhost:8080/$METHOD \
	-H "Content-Type: application/x-www-form-urlencoded"
	;;
esac
