package edu.cwru.eecs395_s16.annotations;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by james on 1/22/16.
 */

@SupportedAnnotationTypes({
        "edu.cwru.eecs395_s16.annotations.NetworkEvent"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class NetworkEventAnnotationProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;
    private Types typeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for(Element element : roundEnv.getElementsAnnotatedWith(NetworkEvent.class)){
            if(element.getKind() == ElementKind.METHOD){
                NetworkEvent annotation = element.getAnnotation(NetworkEvent.class);
                ExecutableElement networkedMethod = (ExecutableElement)element;

                //Check the method name follows the correct conventions
                String methodName = networkedMethod.getSimpleName().toString();
                if(!methodName.matches("[a-z][a-zA-Z]*")){
                    error(networkedMethod,"The method %s needs to start with a lower-case letter, and follow camelCase.",networkedMethod.getSimpleName());
                }

                //Check return type extends Response
                TypeMirror expectedReturnType = elementUtils.getTypeElement("edu.cwru.eecs395_s16.networking.responses.Response").asType();
                TypeMirror currentMirror = networkedMethod.getReturnType();
                while (true) {
                    //Get TypeKind and check for none - we have reached the top of the inheritance tree.
                    TypeKind kind = currentMirror.getKind();
                    if(kind == TypeKind.NONE){
                        error(networkedMethod,"The method %s should return a type that extends from Response.",networkedMethod.getSimpleName());
                        //Move on to typechecking for parameters
                        break;
                    }
                    //Check typekind for match to Response
                    if(currentMirror == expectedReturnType){
                        //Great! It matches Response
                        break;
                    }
                    //Move up the tree
                    TypeElement ele = (TypeElement)typeUtils.asElement(currentMirror);
                    currentMirror = ele.getSuperclass();
                }

                //Check Method Parameters
                List<? extends VariableElement> methodParams = networkedMethod.getParameters();
                TypeMirror expectedDataType = elementUtils.getTypeElement("edu.cwru.eecs395_s16.networking.requests.RequestData").asType();
                TypeMirror playerType = elementUtils.getTypeElement("edu.cwru.eecs395_s16.core.Player").asType();
                TypeMirror clientType = elementUtils.getTypeElement("com.corundumstudio.socketio.SocketIOClient").asType();
                if(annotation.mustAuthenticate()){
                    //Check for the second parameter to be the Player type
                    switch(methodParams.size()){
                        case 1:
                            error(networkedMethod, "The method %s must have a Player object for the second parameter.", networkedMethod.getSimpleName());
                            break;
                        case 2:
                            //Check types here.
                            VariableElement dataParam = methodParams.get(0);
                            if(!typeUtils.isAssignable(dataParam.asType(),expectedDataType)){
                                error(networkedMethod, "The method %s must have a data object that implements the RequestData interface for its first parameter.",networkedMethod.getSimpleName());
                            }

                             VariableElement playerParam = methodParams.get(1);
                            if(!typeUtils.isAssignable(playerParam.asType(),playerType)){
                                error(networkedMethod, "You must have a Player object for the second parameter of the method %s.", networkedMethod.getSimpleName());
                            }
                            break;
                        default:
                            //Here is where checking for injectable types would occur. Probably.
                            error(networkedMethod, "You must have an object that implements the RequestData interface as the first parameter and a Player object for the second parameter of the method %s.", networkedMethod.getSimpleName());
                            break;
                    }
                } else {
                    switch(methodParams.size()){
                        case 0:
                            error(networkedMethod, "The method %s must have a data object as its first parameter.", networkedMethod.getSimpleName());
                            break;
                        case 1:
                            //This is the case where we only have the data object as the first parameter.
                            VariableElement dataParam = methodParams.get(0);
                            if(!typeUtils.isAssignable(dataParam.asType(),expectedDataType)){
                                error(networkedMethod, "The method %s must have a data object that implements the RequestData interface for its first parameter.",networkedMethod.getSimpleName());
                            }
                            break;
                        case 2:
                            //This is the case where the first parameter should be a requestdata object, and the second can be the socket.io client class. Check for that here.
                            VariableElement dataParam1 = methodParams.get(0);
                            if(!typeUtils.isAssignable(dataParam1.asType(),expectedDataType)){
                                error(networkedMethod, "The method %s must have a data object that implements the RequestData interface for its first parameter.",networkedMethod.getSimpleName());
                            }
                            //Check for the client type as the second parameter
                            if(methodParams.size() > 1) {
                                VariableElement clientParam = methodParams.get(1);
                                if (!typeUtils.isAssignable(clientParam.asType(), clientType)) {
                                    error(networkedMethod, "The second parameter for a non-authenticated network method must be SocketIOClient.");
                                }
                            }
                            break;
                        default:
                            error(networkedMethod, "The method %s has too many parameters.", networkedMethod.getSimpleName());
                            break;
                    }
                }
            }
        }
        return true;
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}
