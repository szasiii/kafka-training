# Sprawdzanie, czy sieć typu bridge w Docker działa poprawnie

## Utwórz sieć typu bridge

```bash
  docker network create mynetwork
```
```bash
  docker network inspect mynetwork
```
```json
[
  {
    "Name": "mynetwork",
    "Id": "2b2272ed7504c40a99c7146ad7af1760a546a00f2f2aa4c4d9046afbeacbcb3f",
    "Created": "2024-11-24T11:19:27.032741052Z",
    "Scope": "local",
    "Driver": "bridge",
    "EnableIPv6": false,
    "IPAM": {
      "Driver": "default",
      "Options": {},
      "Config": [
        {
          "Subnet": "172.18.0.0/16",
          "Gateway": "172.18.0.1"
        }
      ]
    },
    "Internal": false,
    "Attachable": false,
    "Ingress": false,
    "ConfigFrom": {
      "Network": ""
    },
    "ConfigOnly": false,
    "Containers": {},
    "Options": {},
    "Labels": {}
  }
]
```

