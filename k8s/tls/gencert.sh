KEY_FILE=./key.txt
CERT_FILE=./cert.txt
HOST=webservice.redisstudy.k8s-test.perit.hu
openssl req -x509 -nodes -days 3650 -newkey rsa:2048 -keyout ${KEY_FILE} -out ${CERT_FILE} -subj "/CN=${HOST}/O=${HOST}" -addext "subjectAltName = DNS:${HOST}"
