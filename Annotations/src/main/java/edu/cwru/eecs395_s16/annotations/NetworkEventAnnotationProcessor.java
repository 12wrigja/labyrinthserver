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
                ExecutableElement exeEelement = (ExecutableElement)element;

                //Check return type extends Response
                TypeMirror expectedReturnType = elementUtils.getTypeElement("edu.cwru.eecs395_s16.networking.Response").asType();
                TypeMirror currentMirror = exeEelement.getReturnType();
                while (true) {
                    //Get TypeKind and check for none - we have reached the top of the inheritance tree.
                    TypeKind kind = currentMirror.getKind();
                    if(kind == TypeKind.NONE){
                        error(exeEelement,"The method %s should return a type that extends from Response.",exeEelement.getSimpleName());
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
                List<? extends VariableElement> methodParams = exeEelement.getParameters();
                TypeMirror playerType = elementUtils.getTypeElement("edu.cwru.eecs395_s16.core.Player").asType();
                if(annotation.mustAuthenticate()){
                    //Check for the second parameter to be the Player type
                    switch(methodParams.size()){
                        case 0:
                            error(exeEelement, "You must have a data object as the first parameter and a Player object for the second parameter of the method %s.", exeEelement.getSimpleName());
                            break;
                        case 1:
                            error(exeEelement, "You must have a Player object for the second parameter of the method %s.", exeEelement.getSimpleName());
                            break;
                        case 2:
                            //Check types here.
                             VariableElement playerParam = methodParams.get(1);
                            if(!typeUtils.isAssignable(playerParam.asType(),playerType)){
                                error(exeEelement, "You must have a Player object for the second parameter of the method %s.", exeEelement.getSimpleName());
                                break;
                            }
                            break;
                        default:
                            //Here is where checking for injectable types would occur. Probably.
                            error(exeEelement, "There can only be one parameter for the method %s.", exeEelement.getSimpleName());
                            break;
                    }
                } else {
                    switch(methodParams.size()){
                        case 0:
                            error(exeEelement, "You must have a data object as the first parameter for the method %s.", exeEelement.getSimpleName());
                            break;
                        case 1:
                            break;
                        default:
                            error(exeEelement, "There can only be one parameter for the method %s.", exeEelement.getSimpleName());
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
