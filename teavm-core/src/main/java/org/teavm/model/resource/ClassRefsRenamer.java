package org.teavm.model.resource;

import java.util.Map;
import org.teavm.codegen.Mapper;
import org.teavm.javascript.ni.Rename;
import org.teavm.model.*;
import org.teavm.model.instructions.*;

/**
 *
 * @author Alexey Andreev
 */
class ClassRefsRenamer implements InstructionVisitor {
    private Mapper<String, String> classNameMapper;

    public ClassRefsRenamer(Mapper<String, String> classNameMapper) {
        this.classNameMapper = classNameMapper;
    }

    public ClassHolder rename(ClassHolder cls) {
        ClassHolder renamedCls = new ClassHolder(classNameMapper.map(cls.getName()));
        renamedCls.getModifiers().addAll(cls.getModifiers());
        renamedCls.setLevel(cls.getLevel());
        if (cls != null) {
            renamedCls.setParent(classNameMapper.map(cls.getParent()));
        }
        for (MethodHolder method : cls.getMethods()) {
            renamedCls.addMethod(rename(method));
        }
        for (FieldHolder field : cls.getFields().toArray(new FieldHolder[0])) {
            field.getOwner().removeField(field);
            renamedCls.addField(field);
        }
        rename(cls.getAnnotations(), renamedCls.getAnnotations());
        for (String iface : cls.getInterfaces()) {
            renamedCls.getInterfaces().add(classNameMapper.map(iface));
        }
        return renamedCls;
    }

    public MethodHolder rename(MethodHolder method) {
        String methodName = method.getName();
        AnnotationHolder renameAnnot = method.getAnnotations().get(Rename.class.getName());
        if (renameAnnot != null) {
            methodName = renameAnnot.getValues().get("value").getString();
        }
        ValueType[] signature = method.getSignature();
        for (int i = 0; i < signature.length; ++i) {
            signature[i] = rename(signature[i]);
        }
        MethodHolder renamedMethod = new MethodHolder(methodName, signature);
        renamedMethod.getModifiers().addAll(method.getModifiers());
        renamedMethod.setLevel(method.getLevel());
        renamedMethod.setProgram(method.getProgram());
        rename(method.getAnnotations(), renamedMethod.getAnnotations());
        rename(renamedMethod.getProgram());
        return renamedMethod;
    }

    private ValueType rename(ValueType type) {
        if (type instanceof ValueType.Array) {
            ValueType itemType = ((ValueType.Array)type).getItemType();
            return ValueType.arrayOf(rename(itemType));
        } else if (type instanceof ValueType.Object) {
            String className = ((ValueType.Object)type).getClassName();
            return ValueType.object(classNameMapper.map(className));
        } else {
            return type;
        }
    }

    private void rename(AnnotationContainer source, AnnotationContainer target) {
        for (AnnotationHolder annot : source.all()) {
            if (!annot.getType().equals(Rename.class.getName())) {
                target.add(rename(annot));
            }
        }
    }

    private AnnotationHolder rename(AnnotationHolder annot) {
        AnnotationHolder renamedAnnot = new AnnotationHolder(classNameMapper.map(annot.getType()));
        for (Map.Entry<String, AnnotationValue> entry : renamedAnnot.getValues().entrySet()) {
            renamedAnnot.getValues().put(entry.getKey(), entry.getValue());
        }
        return renamedAnnot;
    }

    public void rename(Program program) {
        for (int i = 0; i < program.basicBlockCount(); ++i) {
            BasicBlock basicBlock = program.basicBlockAt(i);
            for (Instruction insn : basicBlock.getInstructions()) {
                insn.acceptVisitor(this);
            }
        }
    }

    @Override
    public void visit(EmptyInstruction insn) {
    }

    @Override
    public void visit(ClassConstantInstruction insn) {
        insn.setConstant(rename(insn.getConstant()));
    }

    @Override
    public void visit(NullConstantInstruction insn) {
    }

    @Override
    public void visit(IntegerConstantInstruction insn) {
    }

    @Override
    public void visit(LongConstantInstruction insn) {
    }

    @Override
    public void visit(FloatConstantInstruction insn) {
    }

    @Override
    public void visit(DoubleConstantInstruction insn) {
    }

    @Override
    public void visit(StringConstantInstruction insn) {
    }

    @Override
    public void visit(BinaryInstruction insn) {
    }

    @Override
    public void visit(NegateInstruction insn) {
    }

    @Override
    public void visit(AssignInstruction insn) {
    }

    @Override
    public void visit(CastInstruction insn) {
        insn.setTargetType(rename(insn.getTargetType()));
    }

    @Override
    public void visit(CastNumberInstruction insn) {
    }

    @Override
    public void visit(BranchingInstruction insn) {
    }

    @Override
    public void visit(BinaryBranchingInstruction insn) {
    }

    @Override
    public void visit(JumpInstruction insn) {
    }

    @Override
    public void visit(SwitchInstruction insn) {
    }

    @Override
    public void visit(ExitInstruction insn) {
    }

    @Override
    public void visit(RaiseInstruction insn) {
    }

    @Override
    public void visit(ConstructArrayInstruction insn) {
        insn.setItemType(rename(insn.getItemType()));
    }

    @Override
    public void visit(ConstructInstruction insn) {
        insn.setType(classNameMapper.map(insn.getType()));
    }

    @Override
    public void visit(ConstructMultiArrayInstruction insn) {
        insn.setItemType(rename(insn.getItemType()));
    }

    @Override
    public void visit(GetFieldInstruction insn) {
        insn.setClassName(classNameMapper.map(insn.getClassName()));
    }

    @Override
    public void visit(PutFieldInstruction insn) {
        insn.setClassName(classNameMapper.map(insn.getClassName()));
    }

    @Override
    public void visit(ArrayLengthInstruction insn) {
    }

    @Override
    public void visit(CloneArrayInstruction insn) {
    }

    @Override
    public void visit(GetElementInstruction insn) {
    }

    @Override
    public void visit(PutElementInstruction insn) {
    }

    @Override
    public void visit(InvokeInstruction insn) {
        insn.setClassName(classNameMapper.map(insn.getClassName()));
    }

    @Override
    public void visit(IsInstanceInstruction insn) {
        insn.setType(rename(insn.getType()));
    }
}
