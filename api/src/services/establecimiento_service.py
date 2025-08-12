from flask import Blueprint, request, Response, jsonify
from models.establecimiento import Establecimiento
from schemas.establecimiento_schema import EstablecimientosSchema
import requests
from config import DevelopmentConfig
from models.administrador_establecimiento import AdministradorEstablecimiento
from models.oferta import Oferta
from models.evento import Evento
from models.review import Review
from uploads_config import photos
from marshmallow import ValidationError
from flask_jwt_extended import get_jwt_identity, jwt_required

blueprint = Blueprint("Establecimiento", "establecimientos", url_prefix="/establecimientos")

url_oferta = f"{DevelopmentConfig.BASE_URL}/ofertas"
url_evento = f"{DevelopmentConfig.BASE_URL}/eventos"

@blueprint.route("", methods=["POST"])
def crear_establecimiento():
    try:
        # Detectar si es JSON o form-data
        if request.content_type and "application/json" in request.content_type:
            data = request.get_json()
        else:
            data = request.form.to_dict()

        # Manejo de imagen solo si viene como archivo (form-data)
        if 'imagen' in request.files and request.files['imagen'].filename != '':
            filename = photos.save(request.files['imagen'])
            imagen_url = f"/_uploads/photos/{filename}"
        else:
            imagen_url = f"/_uploads/photos/default_establecimiento.png"

        data['imagen_url'] = imagen_url

        # Manejo del campo ambiente (como lista)
        ambiente_str = data.get('ambiente')
        if ambiente_str:
            data['ambiente'] = ambiente_str.split(',') if isinstance(ambiente_str, str) else ambiente_str
        else:
            data['ambiente'] = []

        # Validar e insertar
        schema = EstablecimientosSchema()
        datos_validados = schema.load(data)
        establecimiento = Establecimiento(datos_validados)
        resultado = establecimiento.insertar_establecimiento()

        # Asociar al administrador
        id_establecimiento = str(resultado.get("id"))
        AdministradorEstablecimiento.add_establecimiento_administrador(
            data.get("id_administrador"),
            id_establecimiento
        )

        return jsonify(resultado), 200

    except ValidationError as e:
        errors = e.messages
        first_error_key = next(iter(errors))
        error_message = errors[first_error_key][0]
        return jsonify({"error": error_message}), 400
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"{e}"}), 500


@blueprint.route("/<id>", methods=["DELETE"])
def eliminar_establecimiento(id):
    try:
        respuesta = Establecimiento.eliminar_establecimiento(id)
        id_administrador = respuesta.get("id_administrador")

        id_establecimiento = str(id)

        AdministradorEstablecimiento.del_establecimiento_administrador(id_administrador, id_establecimiento)
        Oferta.del_oferta_establecimiento(id)
        Evento.del_evento_establecimiento(id)
        Review.del_review_establecimiento(id)

        return respuesta, 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado: {e}"}), 500

@blueprint.route("", methods=["GET"])
def consultar_establecimientos():
    try:
        respuesta = Establecimiento.consultar_establecimientos()
        return Response(respuesta, mimetype="application/json"), 200
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al consultar actividades: {e}"}), 500

@blueprint.route("/ordenados", methods=["GET"])
def consultar_establecimientos_ordenados():
    try:
        respuesta = Establecimiento.consultar_establecimientos_ordenados()
        return Response(respuesta, mimetype="application/json"), 200
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al consultar establecimientos: {e}"}), 500

@blueprint.route("<id>", methods=["GET"])
def consultar_establecimiento(id):
    try:
        respuesta = Establecimiento.consultar_establecimiento(id)
        return Response(respuesta, mimetype="application/json"), 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al consultar actividades: {e}"}), 500

@blueprint.route("/nueva_oferta", methods=["POST"])
def add_oferta():
    try:
        # Detectar tipo de contenido y cargar datos
        if request.content_type and "application/json" in request.content_type:
            data = request.get_json()
        else:
            data = request.form.to_dict()

        # Manejo de imagen
        if 'imagen' in request.files and request.files['imagen'].filename != '':
            filename = photos.save(request.files['imagen'])
            data['imagen_url'] = f"/_uploads/photos/{filename}"
        else:
            data['imagen_url'] = f"/_uploads/photos/default_establecimiento.png"

        id_establecimiento = data.get("id_establecimiento")

        response = requests.post(url_oferta, json=data)
        response.raise_for_status()
        respuesta_json = response.json()

        id_oferta = respuesta_json.get("id_oferta")
        if not id_oferta:
            raise ValueError("No se recibió 'id_oferta' en la respuesta del servidor.")

        Establecimiento.add_ofertas_establecimiento(id_establecimiento, id_oferta)
        return respuesta_json, 200

    except requests.HTTPError as e:
        try:
            return jsonify({"error": e.response.json()}), e.response.status_code
        except Exception:
            return jsonify({"error": "Error HTTP sin respuesta JSON", "detalles": str(e)}), e.response.status_code if e.response else 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado: {str(e)}"}), 500


@blueprint.route("/nuevo_evento", methods=["POST"])
def add_evento():
    try:
        # Detectar tipo de contenido y cargar datos
        if request.content_type and "application/json" in request.content_type:
            data = request.get_json()
        else:
            data = request.form.to_dict()

        # Manejo de imagen
        if 'imagen' in request.files and request.files['imagen'].filename != '':
            filename = photos.save(request.files['imagen'])
            data['imagen_url'] = f"/_uploads/photos/{filename}"
        else:
            data['imagen_url'] = f"/_uploads/photos/default_establecimiento.png"

        id_establecimiento = data.get("id_establecimiento")

        response = requests.post(url_evento, json=data)
        response.raise_for_status()
        respuesta_json = response.json()

        id_evento = respuesta_json.get("id_evento")
        if not id_evento:
            raise ValueError("No se recibió 'id_evento' en la respuesta del servidor.")

        Establecimiento.add_evento_establecimiento(id_establecimiento, id_evento)
        return respuesta_json, 200

    except requests.HTTPError as e:
        try:
            return jsonify({"error": e.response.json()}), e.response.status_code
        except Exception:
            return jsonify({"error": "Error HTTP sin respuesta JSON", "detalles": str(e)}), e.response.status_code if e.response else 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado: {str(e)}"}), 500


@blueprint.route("/filtro_personalizado", methods=["GET"])
@jwt_required()
def filtrar():
    id = get_jwt_identity()
    print(id)

    try:
        respuesta = Establecimiento.filtrar_personalizado(id)
        return Response(respuesta, mimetype="application/json"), 200
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al consultar actividades: {e}"}), 500
    
@blueprint.route("/filtrar", methods=["GET"])
def filtrar_and():
    ambientes_solicitados = request.args.getlist("ambiente")

    try:
        respuesta = Establecimiento.filtrar_and(ambientes_solicitados)
        return Response(respuesta, mimetype="application/json"), 200
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al consultar actividades: {e}"}), 500

@blueprint.route("/rating/<id>", methods=["GET"])
def obtener_calificacion(id):
    try:
        respuesta = Establecimiento.media_reviews(id)
        return respuesta, 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al consultar la media: {e}"}), 500

@blueprint.route("/<id>", methods=["PUT"])
def actualizar_establecimiento(id):
    data = request.json

    try:
        respuesta = Establecimiento.actualizar_establecimiento(id, data)
        return respuesta, 200
    except ValueError as e:
        return jsonify({"error": str(e)}), 404
    except RuntimeError as e:
        return jsonify({"error": str(e)}), 500
    except Exception as e:
        return jsonify({"error": f"Error inesperado al consultar la media: {e}"}), 500