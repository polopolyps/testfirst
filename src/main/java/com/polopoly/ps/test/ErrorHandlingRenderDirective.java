package com.polopoly.ps.test;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import com.polopoly.cm.servlet.velocity.directives.RenderDirective;

public class ErrorHandlingRenderDirective extends RenderDirective {

	@Override
	public boolean render(InternalContextAdapter context, Writer writer,
			Node node) throws IOException, ResourceNotFoundException,
			ParseErrorException {
		System.out.println(context.getCurrentTemplateName()  + " " + context.getCurrentResource());
		System.out.println(node.toString());

		try {
			return super.render(context, writer, node);
		} catch (RuntimeException e) {

			throw e;
		}
	}
}

