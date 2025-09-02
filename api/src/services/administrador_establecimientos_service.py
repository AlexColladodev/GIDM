from flask import Blueprint, request, Response, jsonify
from models.administrador_establecimiento import AdministradorEstablecimiento
from schemas.administrador_establecimiento_schema import AdministradorEstablecimientoSchema
from flask_jwt_extended import get_jwt_identity, jwt_required
import requests
from config import DevelopmentConfig
from uploads_config import photos
from marshmallow import ValidationError

blueprint = Blueprint("AdministradorEstablecimiento", "administrador_establecimiento", url_prefix="/administrador_establecimiento")

url = f"{DevelopmentConfig.BASE_URL}/establecimientos"

@blueprint.route("", methods=["POST"])
def crear_administrador_establecimiento():
    try:
        content_type = request.content_type
        if content_type and "application/json" in content_type:
            data = request.get_json()
        else:
            data = request.form.to_dict()

        print(data)

        # Manejo de la imagen (solo si es multipart/form-data)
        if 'imagen' in request.files and request.files['imagen'].filename != '':
            filename = photos.save(request.files['imagen'])
            imagen_url = f"/_uploads/photos/{filename}"
        else:
            imagen_url = f"/_uploads/photos/default.png"

        data['imagen_url'] = imagen_url

        # Validación y creación
        schema = AdministradorEstablecimientoSchema()
        datos_validados = schema.load(data)
        administrador_establecimiento = AdministradorEstablecimiento(datos_validados)
        resultado = administrador_establecimiento.insertar_administrador_establecimiento()
        return jsonify(resultado), 200

    except ValidationError as e:
        errors = e.messages
        first_error_key = next(iter(errors))
        error_message = errors[first_error_key][0]
        return jsonify({"error": error_message}), 400
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception as e:
        return jsonify({"error": f"{e}"}), 500



@blueprint.route("", methods=["DELETE"])
@jwt_required()
def eliminar_administrador_establecimiento():
    admin = get_jwt_identity()
    id = str(admin.get("_id"))

    try:
        respuesta = AdministradorEstablecimiento.eliminar_administrador_establecimiento(id)
        return jsonify(respuesta), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"{e}"}), 500

@blueprint.route("", methods=["GET"])
def consultar_administradores_establecimiento():
    try:
        respuesta = AdministradorEstablecimiento.consultar_administradores_establecimiento()
        return Response(respuesta, mimetype="application/json"), 200
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al consultar administradores de establecimientos: {e}"}), 500

@blueprint.route("/mi_perfil", methods=["GET"])
@jwt_required()
def consultar_administrador_establecimiento():
    id = get_jwt_identity()

    try:
        respuesta = AdministradorEstablecimiento.consultar_administrador_establecimiento(id)
        return Response(respuesta, mimetype="application/json"), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al consultar administradores de establecimientos: {e}"}), 500

@blueprint.route("", methods=["PUT"])
@jwt_required()
def actualizar_administrador_establecimiento():
    data = request.json
    admin = get_jwt_identity()
    id = str(admin.get("_id"))

    try:
        respuesta = AdministradorEstablecimiento.actualizar_administrador_establecimiento(id, data)
        return jsonify(respuesta), 200
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al modificar administradores de establecimientos: {e}"}), 500

@blueprint.route("/nuevo_establecimiento", methods=["POST"])
@jwt_required()
def crear_establecimiento():
    id_administrador = get_jwt_identity()

    try:
        content_type = request.content_type or ""

        if "multipart/form-data" in content_type or "form-data" in content_type:
            form = request.form.to_dict(flat=True)
            ambiente_list = request.form.getlist("ambiente")
            if ambiente_list:
                form["ambiente"] = ",".join(ambiente_list)
            form["id_administrador"] = id_administrador

            files = {}
            if "imagen" in request.files and request.files["imagen"].filename:
                f = request.files["imagen"]
                files["imagen"] = (f.filename, f.stream, f.mimetype or "application/octet-stream")

            resp = requests.post(url, data=form, files=files) 
        else:
            data = request.get_json(silent=True) or {}
            data["id_administrador"] = id_administrador
            resp = requests.post(url, json=data)

        resp.raise_for_status()
        return jsonify(resp.json()), resp.status_code

    except requests.HTTPError as e:
        try:
            err_json = e.response.json()
        except Exception:
            err_json = {"message": e.response.text if e.response is not None else str(e)}
        return jsonify({"error": "Error en la creación de establecimiento", "backend": err_json}), e.response.status_code if e.response else 502
    except Exception as e:
        return jsonify({"error": "Error general en crear establecimiento", "detalles": str(e)}), 500

