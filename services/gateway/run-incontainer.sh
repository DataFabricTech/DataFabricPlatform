HOST=192.168.107.28
PORT=35023

echo "gateway copy start"
sshpass -p mobigen.platform scp -P $PORT -o StrictHostKeyChecking=no build/bin/gateway root@$HOST:/mobigen/gateway
echo "config file copy start"
sshpass -p mobigen.platform ssh -p $PORT -o StrictHostKeyChecking=no root@$HOST mkdir -p /mobigen/gateway/configs
sshpass -p mobigen.platform scp -P $PORT -o StrictHostKeyChecking=no configs/prod.yaml root@$HOST:/mobigen/gateway/configs
echo "run debug mode(use dlv)"
sshpass -p mobigen.platform ssh -p $PORT -o StrictHostKeyChecking=no root@$HOST HOME=/mobigen/gateway dlv --listen=:2345 --headless=true --api-version=2 --accept-multiclient exec /mobigen/gateway/gateway
