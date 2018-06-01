#!/bin/sh

USERNAME=""
FIRSTNAME=""
LASTNAME=""
PESEL=""
DRIVERLICENCE=""
PASSWORD=""
METHOD=""
COOKIEID=""
ROLEID=""
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
    -f|--firstName)
        FIRSTNAME="$2"
        shift # past argument
        shift # past value
        ;;
    -l|--lastName)
        LASTNAME="$2"
        shift # past argument
        shift # past value
        ;;
    --pesel)
        PESEL="$2"
        shift # past argument
        shift # past value
        ;;
    -d|--driver)
        DRIVERLICENCE="$2"
        shift # past argument
        shift # past value
        ;;
    -b|--cookie)
        COOKIEID="$2"
        shift
        shift
        ;;
    -r|--role)
        ROLEID="$2"
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
if [ -z "${FIRSTNAME}" ]; then
	FIRSTNAME="stanislaw"
fi
if [ -z "${LASTNAME}" ]; then
	LASTNAME="baranski"
fi
if [ -z "${PESEL}" ]; then
	PESEL="12345605252"
fi
if [ -z "${DRIVERLICENCE}" ]; then
	DRIVERLICENCE="673476254765"
fi

case $METHOD in
		"signup")
	curl -X POST -v \
	http://localhost:8080/$METHOD \
	-H "Accept: application/json" \
	-H "Content-Type: application/x-www-form-urlencoded" \
	-d "login=${USERNAME}&password=${PASSWORD}&firstName=${FIRSTNAME}&lastName=${LASTNAME}&PESEL=${PESEL}&driverLicence=${DRIVERLICENCE}"
	;;

	"myRoles")
	curl -X GET -v \
	--basic --user $USERNAME:$PASSWORD \
	http://localhost:8080/$METHOD \
	-H "Accept: application/json" \
	-H "Content-Type: application/x-www-form-urlencoded"
	;;

	"login")
	curl -X POST -v -L \
	--basic --user $USERNAME:$PASSWORD \
	http://localhost:8080/$METHOD \
	-H "Accept: application/json" \
	-H "Content-Type: application/x-www-form-urlencoded" \
	-d "roleId=${ROLEID}"
	;;

    grand/*)
    curl -X GET -v \
    -H "Accept: application/json" \
    http://localhost:8080/$METHOD
    ;;

	*)
	curl -X GET -v \
	-b "SESSIONID=${COOKIEID}" \
	-H "Accept: application/json" \
	http://localhost:8080/$METHOD
	;;
esac
